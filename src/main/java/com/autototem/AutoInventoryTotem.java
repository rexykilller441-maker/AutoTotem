package com.autototem;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;

public class AutoInventoryTotem implements ClientModInitializer {

    public static boolean ENABLE_OFFHAND = true;
    public static boolean ENABLE_HOTBAR = true;
    public static boolean ENABLE_DOUBLE_TOTEM = true;

    private static int CONFIGURED_HOTBAR_SLOT = 0;

    private static int DOUBLE_HAND_DELAY = 5;
    private static int OPEN_INVENTORY_DELAY = 5;
    private static int SWITCH_TOTEMS_DELAY = 5;
    private static int CLOSE_INVENTORY_DELAY = 5;

    private static boolean enabled = true;

    private final TotemManager totemManager = new TotemManager();

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> totemManager.tick(client));
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            TotemCommand.register(dispatcher);
        });
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void toggle() {
        enabled = !enabled;
    }

    public static void setEnabled(boolean state) {
        enabled = state;
    }

    public static int getConfiguredSlot() {
        return CONFIGURED_HOTBAR_SLOT;
    }

    public static void setConfiguredSlot(int slot) {
        CONFIGURED_HOTBAR_SLOT = slot;
    }

    public static int getDoubleHandDelay() {
        return DOUBLE_HAND_DELAY;
    }

    public static int getOpenInventoryDelay() {
        return OPEN_INVENTORY_DELAY;
    }

    public static int getSwitchTotemsDelay() {
        return SWITCH_TOTEMS_DELAY;
    }

    public static int getCloseInventoryDelay() {
        return CLOSE_INVENTORY_DELAY;
    }
}
