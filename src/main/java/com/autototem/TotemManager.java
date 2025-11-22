package com.autototem;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;

public class TotemManager {

    private enum State {
        IDLE,
        WAITING_DOUBLE_HAND,
        OPENING_INVENTORY,
        WAITING_OPEN,
        SWITCHING_TOTEMS,
        CLOSING_INVENTORY,
        WAITING_CLOSE
    }

    private State state = State.IDLE;
    private int ticks = 0;

    private boolean needOffhand = false;
    private boolean needHotbar = false;

    public void tick(MinecraftClient client) {
        if (!AutoInventoryTotem.isEnabled()) return;
        if (client.player == null || client.world == null) return;

        switch (state) {
            case IDLE -> check(client);
            case WAITING_DOUBLE_HAND -> waitDoubleHand(client);
            case OPENING_INVENTORY -> openInventory(client);
            case WAITING_OPEN -> waitInventoryOpen(client);
            case SWITCHING_TOTEMS -> performSwitch(client);
            case CLOSING_INVENTORY -> closeInventory(client);
            case WAITING_CLOSE -> waitClose(client);
        }
    }

    private void check(MinecraftClient client) {
        PlayerInventory inv = client.player.getInventory();

        boolean offTotem = inv.offHand.get(0).isOf(Items.TOTEM_OF_UNDYING);
        boolean hotTotem = inv.getStack(AutoInventoryTotem.getConfiguredSlot()).isOf(Items.TOTEM_OF_UNDYING);

        needOffhand = AutoInventoryTotem.ENABLE_OFFHAND && !offTotem;
        needHotbar = AutoInventoryTotem.ENABLE_HOTBAR && !hotTotem;

        if (!(needOffhand || needHotbar)) return;

        if (!hasTotem(inv)) return;

        state = State.WAITING_DOUBLE_HAND;
        ticks = 0;
    }

    private boolean hasTotem(PlayerInventory inv) {
        for (int i = 9; i < 36; i++)
            if (inv.getStack(i).isOf(Items.TOTEM_OF_UNDYING)) return true;
        return false;
    }

    private void waitDoubleHand(MinecraftClient client) {
        ticks++;
        if (ticks >= AutoInventoryTotem.getDoubleHandDelay()) {
            state = State.OPENING_INVENTORY;
            ticks = 0;
        }
    }

    private void openInventory(MinecraftClient client) {
        client.setScreen(new InventoryScreen(client.player));
        state = State.WAITING_OPEN;
        ticks = 0;
    }

    private void waitInventoryOpen(MinecraftClient client) {
        ticks++;
        if (ticks >= AutoInventoryTotem.getOpenInventoryDelay()) {
            state = State.SWITCHING_TOTEMS;
            ticks = 0;
        }
    }

    private int findTotem(PlayerInventory inv) {
        for (int i = 9; i < 36; i++)
            if (inv.getStack(i).isOf(Items.TOTEM_OF_UNDYING)) return i;
        return -1;
    }

    private void performSwitch(MinecraftClient client) {
        PlayerInventory inv = client.player.getInventory();

        int syncId = client.player.playerScreenHandler.syncId;

        // OFFHAND
        if (needOffhand) {
            int totSlot = findTotem(inv);
            if (totSlot != -1) {
                click(client, syncId, totSlot);
                click(client, syncId, 45);
                clickBack(client, syncId, totSlot);
            }
            needOffhand = false;
        }

        // HOTBAR
        if (needHotbar) {
            int totSlot = findTotem(inv);
            if (totSlot != -1) {
                int hot = AutoInventoryTotem.getConfiguredSlot();
                click(client, syncId, totSlot);
                click(client, syncId, hot);
                clickBack(client, syncId, totSlot);
            }
            needHotbar = false;
        }

        state = State.CLOSING_INVENTORY;
        ticks = 0;
    }

    private void click(MinecraftClient c, int syncId, int slot) {
        c.interactionManager.clickSlot(syncId, slot, 0, SlotActionType.PICKUP, c.player);
    }

    private void clickBack(MinecraftClient c, int syncId, int slot) {
        if (!c.player.currentScreenHandler.getCursorStack().isEmpty()) {
            click(c, syncId, slot);
        }
    }

    private void closeInventory(MinecraftClient client) {
        client.player.closeScreen();
        state = State.WAITING_CLOSE;
        ticks = 0;
    }

    private void waitClose(MinecraftClient client) {
        ticks++;
        if (ticks >= AutoInventoryTotem.getCloseInventoryDelay()) {
            state = State.IDLE;
            ticks = 0;
        }
    }
}
