package com.autototem;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.SerializedName;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigManager {

    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("autototem.json");
    private static final Gson GSON = AutoInventoryTotem.GSON;

    public static class ConfigData {
        @SerializedName("enabled")
        public boolean enabled = true;

        @SerializedName("configured_slot")
        public int configuredSlot = 5;

        @SerializedName("doublehand_delay")
        public int doubleHandDelay = 2;

        @SerializedName("openinventory_delay")
        public int openInventoryDelay = 3;

        @SerializedName("switchtotems_delay")
        public int switchTotemsDelay = 2;

        @SerializedName("closeinventory_delay")
        public int closeInventoryDelay = 2;

        @SerializedName("mode")
        public String mode = "fast"; // fast | semi | visible
    }

    public static ConfigData current = new ConfigData();

    public static void loadConfig() {
        try {
            if (Files.exists(CONFIG_PATH)) {
                try (Reader r = Files.newBufferedReader(CONFIG_PATH)) {
                    ConfigData d = GSON.fromJson(r, ConfigData.class);
                    if (d != null) current = d;
                }
            } else {
                saveConfig(); // create default
            }
        } catch (JsonSyntaxException | IOException e) {
            // fallback to defaults
            current = new ConfigData();
        }

        // Apply loaded config to runtime
        AutoInventoryTotem.setModEnabled(current.enabled);
        AutoInventoryTotem.setConfiguredSlot(current.configuredSlot);
        AutoInventoryTotem.setDoubleHandDelay(current.doubleHandDelay);
        AutoInventoryTotem.setOpenInventoryDelay(current.openInventoryDelay);
        AutoInventoryTotem.setSwitchTotemsDelay(current.switchTotemsDelay);
        AutoInventoryTotem.setCloseInventoryDelay(current.closeInventoryDelay);

        switch (current.mode.toLowerCase()) {
            case "semi" -> AutoInventoryTotem.setRefillMode(AutoInventoryTotem.Mode.SEMI);
            case "visible" -> AutoInventoryTotem.setRefillMode(AutoInventoryTotem.Mode.VISIBLE);
            default -> AutoInventoryTotem.setRefillMode(AutoInventoryTotem.Mode.FAST);
        }
    }

    public static void saveConfig() {
        // Sync runtime -> config
        current.enabled = AutoInventoryTotem.isModEnabled();
        current.configuredSlot = AutoInventoryTotem.getConfiguredSlot();
        current.doubleHandDelay = AutoInventoryTotem.getDoubleHandDelay();
        current.openInventoryDelay = AutoInventoryTotem.getOpenInventoryDelay();
        current.switchTotemsDelay = AutoInventoryTotem.getSwitchTotemsDelay();
        current.closeInventoryDelay = AutoInventoryTotem.getCloseInventoryDelay();

        AutoInventoryTotem.Mode m = AutoInventoryTotem.getRefillMode();
        current.mode = switch (m) {
            case SEMI -> "semi";
            case VISIBLE -> "visible";
            default -> "fast";
        };

        try {
            if (!Files.exists(CONFIG_PATH.getParent())) Files.createDirectories(CONFIG_PATH.getParent());
            try (Writer w = Files.newBufferedWriter(CONFIG_PATH)) {
                GSON.toJson(current, w);
            }
        } catch (IOException e) {
            AutoInventoryTotem.LOGGER.warn("Failed to save autotoem config: {}", e.toString());
        }
    }
              }
