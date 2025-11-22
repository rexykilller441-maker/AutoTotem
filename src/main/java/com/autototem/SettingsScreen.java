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

        // Enable / Disable
        general.addEntry(entryBuilder.startBooleanToggle(Text.literal("Enable Mod"), AutoInventoryTotem.isModEnabled())
                .setSaveConsumer(val -> { AutoInventoryTotem.setModEnabled(val); ConfigManager.saveConfig(); })
                .build());

        // Mode selector (enum)
        general.addEntry(entryBuilder.startEnumSelector(Text.literal("Refill Animation Mode"), AutoInventoryTotem.Mode.class, AutoInventoryTotem.getRefillMode())
                .setDefaultValue(AutoInventoryTotem.Mode.FAST)
                .setSaveConsumer(val -> { AutoInventoryTotem.setRefillMode(val); ConfigManager.saveConfig(); })
                .build());

        // Hotbar slot (1..9 displayed, stored 0..8)
        general.addEntry(entryBuilder.startIntField(Text.literal("Hotbar Slot (1-9)"), AutoInventoryTotem.getConfiguredSlot() + 1)
                .setSaveConsumer(val -> {
                    try {
                        int parsed = Integer.parseInt(val.toString());
                        parsed = Math.max(1, Math.min(9, parsed));
                        AutoInventoryTotem.setConfiguredSlot(parsed - 1);
                        ConfigManager.saveConfig();
                    } catch (Exception ignored) {}
                })
                .build());

        // Delays
        general.addEntry(entryBuilder.startIntField(Text.literal("Double Hand Delay (ticks)"), AutoInventoryTotem.getDoubleHandDelay())
                .setSaveConsumer(val -> { try { AutoInventoryTotem.setDoubleHandDelay(Integer.parseInt(val.toString())); ConfigManager.saveConfig(); } catch (Exception ignored) {} })
                .build());

        general.addEntry(entryBuilder.startIntField(Text.literal("Open Inventory Delay (ticks)"), AutoInventoryTotem.getOpenInventoryDelay())
                .setSaveConsumer(val -> { try { AutoInventoryTotem.setOpenInventoryDelay(Integer.parseInt(val.toString())); ConfigManager.saveConfig(); } catch (Exception ignored) {} })
                .build());

        general.addEntry(entryBuilder.startIntField(Text.literal("Switch Totems Delay (ticks)"), AutoInventoryTotem.getSwitchTotemsDelay())
                .setSaveConsumer(val -> { try { AutoInventoryTotem.setSwitchTotemsDelay(Integer.parseInt(val.toString())); ConfigManager.saveConfig(); } catch (Exception ignored) {} })
                .build());

        general.addEntry(entryBuilder.startIntField(Text.literal("Close Inventory Delay (ticks)"), AutoInventoryTotem.getCloseInventoryDelay())
                .setSaveConsumer(val -> { try { AutoInventoryTotem.setCloseInventoryDelay(Integer.parseInt(val.toString())); ConfigManager.saveConfig(); } catch (Exception ignored) {} })
                .build());

        return builder.build();
    }
}
