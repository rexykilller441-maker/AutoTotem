package com.autototem;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;

/**
 * TotemManager
 * - Correct screen-handler slot mappings:
 *   * inventory storage slots: 9..35
 *   * hotbar screen slots: 36..44  (use 36 + hotbarIndex)
 *   * offhand screen slot: 45
 */
public class TotemManager {

    private enum State {
        IDLE,
        WAITING_DOUBLE_HAND,
        WAITING_OPEN_INVENTORY,
        WAITING_SWITCH_TOTEMS,
        WAITING_CLOSE_INVENTORY
    }

    private static final int STORAGE_START = 9;   // storage area (in player inventory)
    private static final int STORAGE_END = 35;
    private static final int HOTBAR_SCREEN_BASE = 36; // screen slot base for hotbar
    private static final int OFFHAND_SCREEN_SLOT = 45; // raw GUI slot for offhand

    private State currentState = State.IDLE;
    private int tickCounter = 0;

    private boolean needsOffhandRefill = false;
    private boolean needsHotbarRefill = false;

    public void tick(MinecraftClient client) {
        if (client == null || client.player == null || client.world == null) return;

        switch (currentState) {
            case IDLE -> checkTotemStatus(client);
            case WAITING_DOUBLE_HAND -> handleDoubleHandDelay(client);
            case WAITING_OPEN_INVENTORY -> handleOpenInventoryDelay(client);
            case WAITING_SWITCH_TOTEMS -> handleSwitchTotemsDelay(client);
            case WAITING_CLOSE_INVENTORY -> handleCloseInventoryDelay(client);
        }
    }

    private void checkTotemStatus(MinecraftClient client) {
        PlayerInventory inventory = client.player.getInventory();

        // Offhand check (player inventory API)
        ItemStack offhandStack = inventory.offHand.get(0);
        boolean hasOffhandTotem = offhandStack.getItem() == Items.TOTEM_OF_UNDYING;

        // Configured hotbar slot (0..8 in player inventory)
        int configuredSlot = AutoInventoryTotem.getConfiguredSlot();
        ItemStack hotbarStack = inventory.getStack(configuredSlot);
        boolean hasHotbarTotem = hotbarStack.getItem() == Items.TOTEM_OF_UNDYING;

        // Nothing to do
        if (hasOffhandTotem && hasHotbarTotem) return;

        // Do we actually have any totems in storage? If none, abort.
        if (findTotemInInventory(inventory) == -1) return;

        needsOffhandRefill = !hasOffhandTotem;
        needsHotbarRefill = !hasHotbarTotem;

        // start the sequence
        currentState = State.WAITING_DOUBLE_HAND;
        tickCounter = 0;
    }

    private int findTotemInInventory(PlayerInventory inventory) {
        // search storage (slots 9..35) for a totem
        for (int i = STORAGE_START; i <= STORAGE_END; i++) {
            ItemStack stack = inventory.getStack(i);
            if (stack.getItem() == Items.TOTEM_OF_UNDYING) return i;
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
            // We don't need to open the GUI on the client to click player inventory slots;
            // proceed to slot operations.
            currentState = State.WAITING_SWITCH_TOTEMS;
            tickCounter = 0;
        }
    }

    private void handleSwitchTotemsDelay(MinecraftClient client) {
        tickCounter++;
        if (tickCounter >= AutoInventoryTotem.getSwitchTotemsDelay()) {
            switchTotems(client);
            currentState = State.WAITING_CLOSE_INVENTORY;
            tickCounter = 0;
        }
    }

    private void handleCloseInventoryDelay(MinecraftClient client) {
        tickCounter++;
        if (tickCounter >= AutoInventoryTotem.getCloseInventoryDelay()) {
            // finalize and reset
            currentState = State.IDLE;
            tickCounter = 0;
            needsOffhandRefill = false;
            needsHotbarRefill = false;
        }
    }

    private void switchTotems(MinecraftClient client) {
        if (client.player == null || client.interactionManager == null) return;

        PlayerInventory inventory = client.player.getInventory();
        int syncId = client.player.playerScreenHandler.syncId;

        // Offhand priority
        if (needsOffhandRefill) {
            int totemSlot = findTotemInInventory(inventory);
            if (totemSlot != -1) {
                safeSwap(syncId, totemSlot, OFFHAND_SCREEN_SLOT, client);
                needsOffhandRefill = false;
            }
        }

        // Hotbar refill (re-find to ensure we didn't use the only totem above)
        if (needsHotbarRefill) {
            int totemSlot = findTotemInInventory(inventory);
            if (totemSlot != -1) {
                int configuredSlot = AutoInventoryTotem.getConfiguredSlot(); // 0..8
                int hotbarScreenSlot = HOTBAR_SCREEN_BASE + configuredSlot; // 36..44
                safeSwap(syncId, totemSlot, hotbarScreenSlot, client);
                needsHotbarRefill = false;
            }
        }
    }

    /**
     * Performs a safe swap: pickup from `fromSlot`, click `toSlot`, and if cursor still has something,
     * put it back into `fromSlot`. This mirrors how a player would swap items in the inventory GUI.
     *
     * Note: clickSlot uses raw screen slot indices (the same indices the server expects).
     */
    private void safeSwap(int syncId, int fromSlot, int toSlot, MinecraftClient client) {
        try {
            // pickup from source
            client.interactionManager.clickSlot(syncId, fromSlot, 0, SlotActionType.PICKUP, client.player);

            // place into destination (may swap with whatever was there)
            client.interactionManager.clickSlot(syncId, toSlot, 0, SlotActionType.PICKUP, client.player);

            // if cursor still contains an item (the previous destination item), put it back to source
            ItemStack cursor = client.player.currentScreenHandler.getCursorStack();
            if (!cursor.isEmpty()) {
                client.interactionManager.clickSlot(syncId, fromSlot, 0, SlotActionType.PICKUP, client.player);
            }
        } catch (Exception e) {
            // defensive: don't crash the client mod if something goes wrong
            // (log via your mod logger if desired)
            AutoInventoryTotem.LOGGER.warn("safeSwap failed (from={}, to={}): {}", fromSlot, toSlot, e.toString());
        }
    }
            }
