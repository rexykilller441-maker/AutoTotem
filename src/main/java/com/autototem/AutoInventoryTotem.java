package com.autototem;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.class_2561;
import net.minecraft.class_304;
import net.minecraft.class_3675;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AutoInventoryTotem implements ClientModInitializer {
    public static final String MOD_ID = "autoinventorytotem";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static class_304 toggleKey;
    private static boolean modEnabled = true;
    private static int configuredSlot = 5;

    private static int doubleHandDelay = 2;
    private static int openInventoryDelay = 3;
    private static int switchTotemsDelay = 2;
    private static int closeInventoryDelay = 2;

    private static TotemManager totemManager;

    @Override
    public void onInitializeClient() {
        LOGGER.info("Initializing AutoInventoryTotem (Client)");

        // Keybind
        toggleKey = KeyBindingHelper.registerKeyBinding(new class_304(
                "key.autoinventorytotem.toggle",
                class_3675.class_307.field_1668,
                GLFW.GLFW_KEY_HOME,
                "category.autoinventorytotem"
        ));

        totemManager = new TotemManager();

        // Tick event
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.field_1724 == null) return;

            while (toggleKey.method_1436()) {
                modEnabled = !modEnabled;
                client.field_1724.method_7353(
                        class_2561.method_43470("§6[AutoTotem] " + (modEnabled ? "§aEnabled" : "§cDisabled")),
                        false
                );
            }

            if (modEnabled) {
                totemManager.tick(client);
            }
        });

        // Commands
        TotemCommand.register();

        LOGGER.info("AutoInventoryTotem loaded successfully.");
    }

    // Getters & Setters
    public static boolean isModEnabled() { return modEnabled; }
    public static int getConfiguredSlot() { return configuredSlot; }
    public static void setConfiguredSlot(int slot) { configuredSlot = slot; }
    public static int getDoubleHandDelay() { return doubleHandDelay; }
    public static void setDoubleHandDelay(int delay) { doubleHandDelay = delay; }
    public static int getOpenInventoryDelay() { return openInventoryDelay; }
    public static void setOpenInventoryDelay(int delay) { openInventoryDelay = delay; }
    public static int getSwitchTotemsDelay() { return switchTotemsDelay; }
    public static void setSwitchTotemsDelay(int delay) { switchTotemsDelay = delay; }
    public static int getCloseInventoryDelay() { return closeInventoryDelay; }
    public static void setCloseInventoryDelay(int delay) { closeInventoryDelay = delay; }
}
