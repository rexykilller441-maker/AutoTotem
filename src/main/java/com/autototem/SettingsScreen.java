package com.autototem;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class SettingsScreen {

    public static Screen create(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Text.literal("AutoInventoryTotem Settings"));

        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        ConfigCategory general = builder.getOrCreateCategory(Text.literal("General"));

        // Enable
        general.addEntry(entryBuilder.startBooleanToggle(Text.literal("Enable Mod"), AutoInventoryTotem.isModEnabled())
                .setSaveConsumer(val -> { AutoInventoryTotem.setModEnabled(val); ConfigManager.saveConfig(); })
                .build());

        // Mode selection (enum)
        general.addEntry(entryBuilder.startEnumSelector(Text.literal("Refill Animation Mode"), AutoInventoryTotem.Mode.class, AutoInventoryTotem.getRefillMode())
                .setDefaultValue(AutoInventoryTotem.Mode.FAST)
                .setSaveConsumer(val -> { AutoInventoryTotem.setRefillMode(val); ConfigManager.saveConfig(); })
                .build());

        // Hotbar slot (0..8 shown as 1..9)
        general.addEntry(entryBuilder.startIntField(Text.literal("Hotbar Slot (1-9)"), AutoInventoryTotem.getConfiguredSlot() + 1)
                .setSaveConsumer(val -> {
                    try {
                        int parsed = Math.max(1, Math.min(9, Integer.parseInt(val.toString())));
                        AutoInventoryTotem.setConfiguredSlot(parsed - 1);
                        ConfigManager.saveConfig();
                    } catch (Exception ignored) {}
                })
                .build());

        // Delays
        general.addEntry(entryBuilder.startIntField(Text.literal("Double Hand Delay (ticks)"), AutoInventoryTotem.getDoubleHandDelay())
                .setSaveConsumer(val -> { AutoInventoryTotem.setDoubleHandDelay(Integer.parseInt(val.toString())); ConfigManager.saveConfig(); })
                .build());

        general.addEntry(entryBuilder.startIntField(Text.literal("Open Inventory Delay (ticks)"), AutoInventoryTotem.getOpenInventoryDelay())
                .setSaveConsumer(val -> { AutoInventoryTotem.setOpenInventoryDelay(Integer.parseInt(val.toString())); ConfigManager.saveConfig(); })
                .build());

        general.addEntry(entryBuilder.startIntField(Text.literal("Switch Totems Delay (ticks)"), AutoInventoryTotem.getSwitchTotemsDelay())
                .setSaveConsumer(val -> { AutoInventoryTotem.setSwitchTotemsDelay(Integer.parseInt(val.toString())); ConfigManager.saveConfig(); })
                .build());

        general.addEntry(entryBuilder.startIntField(Text.literal("Close Inventory Delay (ticks)"), AutoInventoryTotem.getCloseInventoryDelay())
                .setSaveConsumer(val -> { AutoInventoryTotem.setCloseInventoryDelay(Integer.parseInt(val.toString())); ConfigManager.saveConfig(); })
                .build());

        return builder.build();
    }
}
