package com.autototem;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AutoInventoryTotem implements ClientModInitializer {
    public static final String MOD_ID = "autoinventorytotem";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public enum Mode {
        FAST,   // no visible GUI
        SEMI,   // quick open -> swap -> quick close
        VISIBLE // open -> wait -> swap -> wait -> close (visible)
    }

    // Keybind
    private static KeyBinding toggleKey;

    // Runtime state (defaults) - config-manager will overwrite if config exists
    private static boolean modEnabled = true;
    private static int configuredSlot = 5; // 0..8 (0-indexed)
    private static int doubleHandDelay = 2;
    private static int openInventoryDelay = 3;
    private static int switchTotemsDelay = 2;
    private static int closeInventoryDelay = 2;
    private static Mode refillMode = Mode.FAST;

    private static TotemManager totemManager;

    // Shared Gson
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    @Override
    public void onInitializeClient() {
        LOGGER.info("Initializing AutoInventoryTotem");

        // Load config (if present)
        try {
            ConfigManager.loadConfig();
        } catch (Exception e) {
            LOGGER.warn("Failed to load AutoTotem config, using defaults: {}", e.toString());
        }

        // Register toggle keybinding (Home)
        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.autoinventorytotem.toggle",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_HOME,
                "category.autoinventorytotem"
        ));

        // Initialize totem manager
        totemManager = new TotemManager();

        // Tick listener
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;

            while (toggleKey.wasPressed()) {
                modEnabled = !modEnabled;
                client.player.sendMessage(Text.literal("§6[AutoTotem] " + (modEnabled ? "§aEnabled" : "§cDisabled")), false);
                ConfigManager.saveConfig();
            }

            if (modEnabled) {
                totemManager.tick(client);
            }
        });

        // Register commands
        TotemCommand.register();

        LOGGER.info("AutoInventoryTotem initialized (mode={})", refillMode.name());
    }

    // Getter / setters (persist via ConfigManager)
    public static boolean isModEnabled() { return modEnabled; }
    public static void setModEnabled(boolean enabled) { modEnabled = enabled; ConfigManager.saveConfig(); }

    public static int getConfiguredSlot() { return configuredSlot; }
    public static void setConfiguredSlot(int slot) { configuredSlot = Math.max(0, Math.min(8, slot)); ConfigManager.saveConfig(); }

    public static int getDoubleHandDelay() { return doubleHandDelay; }
    public static void setDoubleHandDelay(int delay) { doubleHandDelay = Math.max(0, delay); ConfigManager.saveConfig(); }

    public static int getOpenInventoryDelay() { return openInventoryDelay; }
    public static void setOpenInventoryDelay(int delay) { openInventoryDelay = Math.max(0, delay); ConfigManager.saveConfig(); }

    public static int getSwitchTotemsDelay() { return switchTotemsDelay; }
    public static void setSwitchTotemsDelay(int delay) { switchTotemsDelay = Math.max(0, delay); ConfigManager.saveConfig(); }

    public static int getCloseInventoryDelay() { return closeInventoryDelay; }
    public static void setCloseInventoryDelay(int delay) { closeInventoryDelay = Math.max(0, delay); ConfigManager.saveConfig(); }

    public static Mode getRefillMode() { return refillMode; }
    public static void setRefillMode(Mode mode) { if (mode != null) refillMode = mode; ConfigManager.saveConfig(); }
}
