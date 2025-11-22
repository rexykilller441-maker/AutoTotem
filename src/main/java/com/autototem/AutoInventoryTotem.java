package com.autototem;

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
    
    private static KeyBinding toggleKey;
    private static boolean modEnabled = true;
    private static int configuredSlot = 5; // Slot 6 in-game (0-indexed)
    
    // Delay settings in ticks
    private static int doubleHandDelay = 2;
    private static int openInventoryDelay = 3;
    private static int switchTotemsDelay = 2;
    private static int closeInventoryDelay = 2;
    
    private static TotemManager totemManager;

    @Override
    public void onInitializeClient() {
        LOGGER.info("Initializing AutoInventoryTotem mod");
        
        // Register toggle keybinding (Home key)
        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.autoinventorytotem.toggle",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_HOME,
            "category.autoinventorytotem"
        ));
        
        // Initialize totem manager
        totemManager = new TotemManager();
        
        // Register client tick event
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;
            
            // Check for toggle key press
            while (toggleKey.wasPressed()) {
                modEnabled = !modEnabled;
                client.player.sendMessage(
                    Text.literal("§6[AutoTotem] " + (modEnabled ? "§aEnabled" : "§cDisabled")),
                    false
                );
            }
            
            // Run totem check if enabled
            if (modEnabled) {
                totemManager.tick(client);
            }
        });
        
        // Register command
        TotemCommand.register();
        
        LOGGER.info("AutoInventoryTotem mod initialized successfully");
    }
    
    public static boolean isModEnabled() {
        return modEnabled;
    }
    
    public static int getConfiguredSlot() {
        return configuredSlot;
    }
    
    public static void setConfiguredSlot(int slot) {
        configuredSlot = slot;
    }
    
    public static int getDoubleHandDelay() {
        return doubleHandDelay;
    }
    
    public static void setDoubleHandDelay(int delay) {
        doubleHandDelay = delay;
    }
    
    public static int getOpenInventoryDelay() {
        return openInventoryDelay;
    }
    
    public static void setOpenInventoryDelay(int delay) {
        openInventoryDelay = delay;
    }
    
    public static int getSwitchTotemsDelay() {
        return switchTotemsDelay;
    }
    
    public static void setSwitchTotemsDelay(int delay) {
        switchTotemsDelay = delay;
    }
    
    public static int getCloseInventoryDelay() {
        return closeInventoryDelay;
    }
    
    public static void setCloseInventoryDelay(int delay) {
        closeInventoryDelay = delay;
    }
}
