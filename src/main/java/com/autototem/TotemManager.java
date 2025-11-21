package com.autototem;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;

public class TotemManager {
    private enum State {
        IDLE,
        WAITING_DOUBLE_HAND,
        WAITING_OPEN_INVENTORY,
        WAITING_SWITCH_TOTEMS,
        WAITING_CLOSE_INVENTORY
    }
    
    private State currentState = State.IDLE;
    private int tickCounter = 0;
    private boolean needsOffhandRefill = false;
    private boolean needsHotbarRefill = false;
    
    public void tick(MinecraftClient client) {
        if (client.player == null || client.world == null) return;
        
        switch (currentState) {
            case IDLE:
                checkTotemStatus(client);
                break;
            case WAITING_DOUBLE_HAND:
                handleDoubleHandDelay(client);
                break;
            case WAITING_OPEN_INVENTORY:
                handleOpenInventoryDelay(client);
                break;
            case WAITING_SWITCH_TOTEMS:
                handleSwitchTotemsDelay(client);
                break;
            case WAITING_CLOSE_INVENTORY:
                handleCloseInventoryDelay(client);
                break;
        }
    }
    
    private void checkTotemStatus(MinecraftClient client) {
        PlayerInventory inventory = client.player.getInventory();
        
        // Check offhand
        ItemStack offhandStack = inventory.offHand.get(0);
        boolean hasOffhandTotem = offhandStack.getItem() == Items.TOTEM_OF_UNDYING;
        
        // Check configured hotbar slot
        int configuredSlot = AutoInventoryTotem.getConfiguredSlot();
        ItemStack hotbarStack = inventory.getStack(configuredSlot);
        boolean hasHotbarTotem = hotbarStack.getItem() == Items.TOTEM_OF_UNDYING;
        
        // If either slot is missing a totem, start the refill process
        if (!hasOffhandTotem || !hasHotbarTotem) {
            needsOffhandRefill = !hasOffhandTotem;
            needsHotbarRefill = !hasHotbarTotem;
            
            // Start with double hand delay
            currentState = State.WAITING_DOUBLE_HAND;
            tickCounter = 0;
        }
    }
    
    private void handleDoubleHandDelay(MinecraftClient client) {
        tickCounter++;
        if (tickCounter >= AutoInventoryTotem.getDoubleHandDelay()) {
            currentState = State.WAITING_OPEN_INVENTORY;
            tickCounter = 0;
        }
    }
    
    private void handleOpenInventoryDelay(MinecraftClient client) {
        tickCounter++;
        if (tickCounter >= AutoInventoryTotem.getOpenInventoryDelay()) {
            // Open inventory
            if (client.player != null) {
                client.player.networkHandler.sendPacket(
                    new net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket(
                        client.player,
                        net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket.Mode.OPEN_INVENTORY
                    )
                );
            }
            currentState = State.WAITING_SWITCH_TOTEMS;
            tickCounter = 0;
        }
    }
    
    private void handleSwitchTotemsDelay(MinecraftClient client) {
        tickCounter++;
        if (tickCounter >= AutoInventoryTotem.getSwitchTotemsDelay()) {
            // Perform totem switching
            switchTotems(client);
            currentState = State.WAITING_CLOSE_INVENTORY;
            tickCounter = 0;
        }
    }
    
    private void handleCloseInventoryDelay(MinecraftClient client) {
        tickCounter++;
        if (tickCounter >= AutoInventoryTotem.getCloseInventoryDelay()) {
            // Close inventory by sending close packet
            if (client.player != null && client.player.currentScreenHandler != null) {
                client.player.closeHandledScreen();
            }
            currentState = State.IDLE;
            tickCounter = 0;
            needsOffhandRefill = false;
            needsHotbarRefill = false;
        }
    }
    
    private void switchTotems(MinecraftClient client) {
        PlayerInventory inventory = client.player.getInventory();
        
        // Find totems in inventory (slots 9-35 are storage inventory)
        for (int i = 9; i < 36; i++) {
            ItemStack stack = inventory.getStack(i);
            if (stack.getItem() == Items.TOTEM_OF_UNDYING) {
                // Priority: Offhand first
                if (needsOffhandRefill) {
                    // Move totem to offhand (slot 45)
                    client.interactionManager.clickSlot(
                        client.player.playerScreenHandler.syncId,
                        i,
                        0,
                        SlotActionType.PICKUP,
                        client.player
                    );
                    client.interactionManager.clickSlot(
                        client.player.playerScreenHandler.syncId,
                        45,
                        0,
                        SlotActionType.PICKUP,
                        client.player
                    );
                    // If there's an item in cursor, put it back
                    if (!client.player.currentScreenHandler.getCursorStack().isEmpty()) {
                        client.interactionManager.clickSlot(
                            client.player.playerScreenHandler.syncId,
                            i,
                            0,
                            SlotActionType.PICKUP,
                            client.player
                        );
                    }
                    needsOffhandRefill = false;
                    
                    // Check if we still need to refill hotbar
                    if (!needsHotbarRefill) {
                        break;
                    }
                    continue;
                }
                
                // Then hotbar slot
                if (needsHotbarRefill) {
                    int configuredSlot = AutoInventoryTotem.getConfiguredSlot();
                    // Move totem to configured hotbar slot
                    client.interactionManager.clickSlot(
                        client.player.playerScreenHandler.syncId,
                        i,
                        0,
                        SlotActionType.PICKUP,
                        client.player
                    );
                    client.interactionManager.clickSlot(
                        client.player.playerScreenHandler.syncId,
                        configuredSlot,
                        0,
                        SlotActionType.PICKUP,
                        client.player
                    );
                    // If there's an item in cursor, put it back
                    if (!client.player.currentScreenHandler.getCursorStack().isEmpty()) {
                        client.interactionManager.clickSlot(
                            client.player.playerScreenHandler.syncId,
                            i,
                            0,
                            SlotActionType.PICKUP,
                            client.player
                        );
                    }
                    needsHotbarRefill = false;
                    break;
                }
            }
        }
    }
}
