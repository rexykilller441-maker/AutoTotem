package com.autototem;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

public class AutoInventoryTotem implements ClientModInitializer {
    public static final String MOD_ID = "autoinventorytotem";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public enum Mode {
        FAST,   // fast (no visible GUI)
        SEMI,   // invisible/semi visible (very quick GUI)
        VISIBLE // visible (slow, human-like)
    }

    // Keybind
    private static KeyBinding toggleKey;

    // Runtime state (defaults)
    private static boolean modEnabled = true;
    private static int configuredSlot = 5; // 0-indexed (0..8)
    private static int doubleHandDelay = 2;
    private static int openInventoryDelay = 3;
    private static int switchTotemsDelay = 2;
    private static int closeInventoryDelay = 2;
    private static Mode refillMode = Mode.FAST;

    private static TotemManager totemManager;

    // Gson instance used by ConfigManager too (optional)
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    @Override
    public void onInitializeClient() {
        LOGGER.info("Initializing AutoInventoryTotem mod");

        // Load config
        try {
            ConfigManager.loadConfig();
        } catch (Exception e) {
            LOGGER.warn("Failed to load config, using defaults: {}", e.toString());
        }

        // Register toggle keybinding (Home)
        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.autoinventorytotem.toggle",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_HOME,
                "category.autoinventorytotem"
        ));

        // Init manager
        totemManager = new TotemManager();

        // Tick event
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;

            while (toggleKey.wasPressed()) {
                modEnabled = !modEnabled;
                client.player.sendMessage(
                        Text.literal("§6[AutoTotem] " + (modEnabled ? "§aEnabled" : "§cDisabled")),
                        false
                );
                ConfigManager.saveConfig();
            }

            if (modEnabled) {
                totemManager.tick(client);
            }
        });

        // Commands (register)
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            TotemCommand.register();
        });

        LOGGER.info("AutoInventoryTotem initialized (mode={})", refillMode);
    }

    // Config-backed getters & setters (save on change)
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

    // Expose path for config (useful for debugging)
    public static Path getConfigPath() {
        return FabricLoader.getInstance().getConfigDir().resolve("autototem.json");
    }
                                                      }
