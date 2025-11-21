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
    private int totemSlotInInventory = -1;
    
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
        
        // Check offhand (slot 40 in player inventory)
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
            
            // Check if totems are available in inventory
            if (findTotemInInventory(inventory) == -1) {
                // No totems available, do nothing
                return;
            }
            
            // Start with double hand delay
            currentState = State.WAITING_DOUBLE_HAND;
            tickCounter = 0;
        }
    }
    
    private int findTotemInInventory(PlayerInventory inventory) {
        // Search main inventory (slots 9-35) for totems
        for (int i = 9; i < 36; i++) {
            ItemStack stack = inventory.getStack(i);
            if (stack.getItem() == Items.TOTEM_OF_UNDYING) {
                return i;
            }
        }
        return -1;
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
            // We don't actually need to open a GUI, just prepare for slot operations
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
            // Finalize
            currentState = State.IDLE;
            tickCounter = 0;
            needsOffhandRefill = false;
            needsHotbarRefill = false;
        }
    }
    
    private void switchTotems(MinecraftClient client) {
        if (client.player == null || client.interactionManager == null) return;
        
        PlayerInventory inventory = client.player.getInventory();
        
        // Priority: Offhand first
        if (needsOffhandRefill) {
            int totemSlot = findTotemInInventory(inventory);
            if (totemSlot != -1) {
                // Swap totem from inventory to offhand
                // Using screen handler slot indices:
                // Offhand is slot 45 in the player screen handler
                // Inventory slots 9-35 map to screen handler slots 9-35
                
                client.interactionManager.clickSlot(
                    client.player.playerScreenHandler.syncId,
                    totemSlot,
                    0,
                    SlotActionType.PICKUP,
                    client.player
                );
                
                client.interactionManager.clickSlot(
                    client.player.playerScreenHandler.syncId,
                    45, // Offhand slot in screen handler
                    0,
                    SlotActionType.PICKUP,
                    client.player
                );
                
                // Put back any item that was in offhand
                ItemStack cursorStack = client.player.currentScreenHandler.getCursorStack();
                if (!cursorStack.isEmpty()) {
                    client.interactionManager.clickSlot(
                        client.player.playerScreenHandler.syncId,
                        totemSlot,
                        0,
                        SlotActionType.PICKUP,
                        client.player
                    );
                }
                
                needsOffhandRefill = false;
            }
        }
        
        // Then hotbar slot
        if (needsHotbarRefill) {
            int totemSlot = findTotemInInventory(inventory);
            if (totemSlot != -1) {
                int configuredSlot = AutoInventoryTotem.getConfiguredSlot();
                
                client.interactionManager.clickSlot(
                    client.player.playerScreenHandler.syncId,
                    totemSlot,
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
                
                // Put back any item that was in the slot
                ItemStack cursorStack = client.player.currentScreenHandler.getCursorStack();
                if (!cursorStack.isEmpty()) {
                    client.interactionManager.clickSlot(
                        client.player.playerScreenHandler.syncId,
                        totemSlot,
                        0,
                        SlotActionType.PICKUP,
                        client.player
                    );
                }
                
                needsHotbarRefill = false;
            }
        }
    }
    }
