package com.autototem;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;

public class TotemManager {

    private enum State {
        IDLE,
        WAITING_DOUBLEHAND,
        OPENING_INVENTORY,
        WAITING_OPEN,
        SWITCHING_TOTEMS,
        WAITING_CLOSE,
        CLOSING_INVENTORY
    }

    private State state = State.IDLE;
    private int tickCounter = 0;

    private boolean needsOffhand = false;
    private boolean needsHotbar = false;

    // Screen-handler indices
    private static final int STORAGE_START = 9;   // storage area 9..35
    private static final int STORAGE_END = 35;
    private static final int HOTBAR_SCREEN_BASE = 36; // 36 + hotbarIndex (0..8)
    private static final int OFFHAND_SCREEN_SLOT = 45; // offhand slot

    public void tick(MinecraftClient client) {
        if (client == null || client.player == null || client.world == null) return;

        switch (state) {
            case IDLE -> checkStatus(client);
            case WAITING_DOUBLEHAND -> waitFor(AutoInventoryTotem.getDoubleHandDelay(), State.OPENING_INVENTORY);
            case OPENING_INVENTORY -> openInventoryScreen(client);
            case WAITING_OPEN -> waitFor(AutoInventoryTotem.getOpenInventoryDelay(), State.SWITCHING_TOTEMS);
            case SWITCHING_TOTEMS -> performSwitch(client);
            case WAITING_CLOSE -> waitFor(AutoInventoryTotem.getCloseInventoryDelay(), State.CLOSING_INVENTORY);
            case CLOSING_INVENTORY -> closeInventoryScreen(client);
        }
    }

    private void checkStatus(MinecraftClient client) {
        PlayerInventory inv = client.player.getInventory();

        boolean hasOffhand = inv.offHand.get(0).getItem() == Items.TOTEM_OF_UNDYING;
        boolean hasHotbar = inv.getStack(AutoInventoryTotem.getConfiguredSlot()).getItem() == Items.TOTEM_OF_UNDYING;

        needsOffhand = AutoInventoryTotem.getConfiguredSlot() >= 0 && !hasOffhand; // refill offhand if missing
        needsHotbar = !hasHotbar;

        if (!needsOffhand && !needsHotbar) return;

        // check if any totem in storage
        if (findTotemInStorage(inv) == -1) return;

        // start
        state = State.WAITING_DOUBLEHAND;
        tickCounter = 0;
    }

    private void waitFor(int delay, State next) {
        tickCounter++;
        if (tickCounter >= Math.max(0, delay)) {
            state = next;
            tickCounter = 0;
        }
    }

    private void openInventoryScreen(MinecraftClient client) {
        AutoInventoryTotem.Mode mode = AutoInventoryTotem.getRefillMode();

        if (mode == AutoInventoryTotem.Mode.FAST) {
            // no GUI
            state = State.SWITCHING_TOTEMS;
            tickCounter = 0;
            return;
        }

        // OPEN GUI for SEMI and VISIBLE
        client.setScreen(new InventoryScreen(client.player));
        state = State.WAITING_OPEN;
        tickCounter = 0;
    }

    private void performSwitch(MinecraftClient client) {
        if (client.interactionManager == null || client.player == null) {
            finish();
            return;
        }

        PlayerInventory inv = client.player.getInventory();
        int syncId = client.player.playerScreenHandler.syncId;

        // Offhand first
        if (needsOffhand) {
            int from = findTotemInStorage(inv);
            if (from != -1) {
                safeSwap(syncId, from, OFFHAND_SCREEN_SLOT, client);
            }
            needsOffhand = false;
        }

        // Hotbar next
        if (needsHotbar) {
            int from = findTotemInStorage(inv);
            if (from != -1) {
                int hotbarIndex = AutoInventoryTotem.getConfiguredSlot(); // 0..8
                int hotbarScreenSlot = HOTBAR_SCREEN_BASE + Math.max(0, Math.min(8, hotbarIndex));
                safeSwap(syncId, from, hotbarScreenSlot, client);
            }
            needsHotbar = false;
        }

        // Decide next state depending on mode
        AutoInventoryTotem.Mode mode = AutoInventoryTotem.getRefillMode();
        if (mode == AutoInventoryTotem.Mode.SEMI) {
            // close quickly
            state = State.CLOSING_INVENTORY;
            tickCounter = 0;
        } else if (mode == AutoInventoryTotem.Mode.VISIBLE) {
            // wait so user can see the swap
            state = State.WAITING_CLOSE;
            tickCounter = 0;
        } else { // FAST
            finish();
        }
    }

    private void closeInventoryScreen(MinecraftClient client) {
        Screen current = client.currentScreen;
        if (current instanceof InventoryScreen) {
            client.setScreen(null);
        }
        finish();
    }

    private void finish() {
        state = State.IDLE;
        tickCounter = 0;
        needsOffhand = false;
        needsHotbar = false;
    }

    private int findTotemInStorage(PlayerInventory inv) {
        for (int i = STORAGE_START; i <= STORAGE_END; i++) {
            ItemStack stack = inv.getStack(i);
            if (stack != null && stack.getItem() == Items.TOTEM_OF_UNDYING) return i;
        }
        return -1;
    }

    private void safeSwap(int syncId, int fromSlot, int toSlot, MinecraftClient client) {
        try {
            client.interactionManager.clickSlot(syncId, fromSlot, 0, SlotActionType.PICKUP, client.player);
            client.interactionManager.clickSlot(syncId, toSlot, 0, SlotActionType.PICKUP, client.player);

            // if cursor contains something, return to source slot
            ItemStack cursor = client.player.currentScreenHandler.getCursorStack();
            if (cursor != null && !cursor.isEmpty()) {
                client.interactionManager.clickSlot(syncId, fromSlot, 0, SlotActionType.PICKUP, client.player);
            }
        } catch (Exception e) {
            AutoInventoryTotem.LOGGER.warn("safeSwap failed (from={}, to={}): {}", fromSlot, toSlot, e.toString());
        }
    }
                                                                     }
