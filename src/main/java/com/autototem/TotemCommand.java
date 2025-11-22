package com.autototem;

import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class TotemCommand {

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {

        dispatcher.register(literal("tt")
                // Toggle entire mod
                .then(literal("toggle")
                        .executes(context -> {
                            AutoInventoryTotem.toggle();
                            context.getSource().sendFeedback("AutoTotem: " +
                                    (AutoInventoryTotem.isEnabled() ? "§aEnabled" : "§cDisabled"));
                            return 1;
                        })
                )

                // Toggle Offhand Mode
                .then(literal("offhand")
                        .executes(context -> {
                            AutoInventoryTotem.ENABLE_OFFHAND = !AutoInventoryTotem.ENABLE_OFFHAND;
                            context.getSource().sendFeedback("Offhand Refill: " +
                                    (AutoInventoryTotem.ENABLE_OFFHAND ? "§aEnabled" : "§cDisabled"));
                            return 1;
                        })
                )

                // Toggle Hotbar Mode
                .then(literal("hotbar")
                        .executes(context -> {
                            AutoInventoryTotem.ENABLE_HOTBAR = !AutoInventoryTotem.ENABLE_HOTBAR;
                            context.getSource().sendFeedback("Hotbar Refill: " +
                                    (AutoInventoryTotem.ENABLE_HOTBAR ? "§aEnabled" : "§cDisabled"));
                            return 1;
                        })
                )

                // Toggle Double Totem Mode
                .then(literal("double")
                        .executes(context -> {
                            AutoInventoryTotem.ENABLE_DOUBLE_TOTEM = !AutoInventoryTotem.ENABLE_DOUBLE_TOTEM;
                            context.getSource().sendFeedback("Double-Totem Mode: " +
                                    (AutoInventoryTotem.ENABLE_DOUBLE_TOTEM ? "§aEnabled" : "§cDisabled"));
                            return 1;
                        })
                )

                // Set hotbar slot
                .then(literal("slot")
                        .then(net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument(
                                        "id", com.mojang.brigadier.arguments.IntegerArgumentType.integer(0, 8))
                                .executes(context -> {
                                    int id = com.mojang.brigadier.arguments.IntegerArgumentType.getInteger(context, "id");
                                    AutoInventoryTotem.setConfiguredSlot(id);
                                    context.getSource().sendFeedback("Totem Hotbar Slot Set To: §e" + id);
                                    return 1;
                                })
                        )
                )
        );
    }
                                        }
