package com.autototem;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

public class TotemCommand {

    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            registerCommand(dispatcher);
        });
    }

    private static void registerCommand(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(ClientCommandManager.literal("tt")
            // enable / disable
            .then(ClientCommandManager.literal("enable")
                .executes(ctx -> toggleMod(ctx, true)))
            .then(ClientCommandManager.literal("disable")
                .executes(ctx -> toggleMod(ctx, false)))

            // gui
            .then(ClientCommandManager.literal("gui")
                .executes(ctx -> {
                    MinecraftClient mc = MinecraftClient.getInstance();
                    mc.setScreen(SettingsScreen.create(mc.currentScreen));
                    ctx.getSource().sendFeedback(Text.literal("§6[AutoTotem] §aOpened Settings GUI"));
                    return 1;
                })
            )

            // mode
            .then(ClientCommandManager.literal("mode")
                .then(ClientCommandManager.literal("fast").executes(ctx -> setMode(ctx, "fast")))
                .then(ClientCommandManager.literal("semi").executes(ctx -> setMode(ctx, "semi")))
                .then(ClientCommandManager.literal("visible").executes(ctx -> setMode(ctx, "visible")))
            )

            // slot
            .then(ClientCommandManager.literal("slot")
                .then(ClientCommandManager.argument("slotNumber", IntegerArgumentType.integer(1, 9))
                    .executes(ctx -> setSlot(ctx))))

            // delays
            .then(ClientCommandManager.literal("delay")
                .then(ClientCommandManager.literal("doublehand")
                    .then(ClientCommandManager.argument("ticks", IntegerArgumentType.integer(0, 100))
                        .executes(context -> setDelay(context, "doublehand"))))
                .then(ClientCommandManager.literal("openinventory")
                    .then(ClientCommandManager.argument("ticks", IntegerArgumentType.integer(0, 100))
                        .executes(context -> setDelay(context, "openinventory"))))
                .then(ClientCommandManager.literal("switchtotems")
                    .then(ClientCommandManager.argument("ticks", IntegerArgumentType.integer(0, 100))
                        .executes(context -> setDelay(context, "switchtotems"))))
                .then(ClientCommandManager.literal("closeinventory")
                    .then(ClientCommandManager.argument("ticks", IntegerArgumentType.integer(0, 100))
                        .executes(context -> setDelay(context, "closeinventory")))))

            // check / status / info / help
            .then(ClientCommandManager.literal("check")
                .executes(ctx -> { ctx.getSource().sendFeedback(Text.literal("Checking now...")); return 1; }))
            .then(ClientCommandManager.literal("status")
                .executes(TotemCommand::showInfo))
            .then(ClientCommandManager.literal("info")
                .executes(TotemCommand::showInfo))
            .then(ClientCommandManager.literal("help")
                .executes(ctx -> { ctx.getSource().sendFeedback(Text.literal(getHelp())); return 1; }))
        );
    }

    private static int setSlot(com.mojang.brigadier.context.CommandContext<FabricClientCommandSource> context) {
        int slotNumber = IntegerArgumentType.getInteger(context, "slotNumber");
        int slotIndex = slotNumber - 1;

        AutoInventoryTotem.setConfiguredSlot(slotIndex);
        context.getSource().sendFeedback(
            Text.literal("§6[AutoTotem] §aConfigured hotbar slot set to: §e" + slotNumber)
        );
        return 1;
    }

    private static int setDelay(com.mojang.brigadier.context.CommandContext<FabricClientCommandSource> context, String delayType) {
        int ticks = IntegerArgumentType.getInteger(context, "ticks");

        switch (delayType) {
            case "doublehand":
                AutoInventoryTotem.setDoubleHandDelay(ticks);
                context.getSource().sendFeedback(Text.literal("§6[AutoTotem] §aDouble hand delay set to: §e" + ticks + " ticks"));
                break;
            case "openinventory":
                AutoInventoryTotem.setOpenInventoryDelay(ticks);
                context.getSource().sendFeedback(Text.literal("§6[AutoTotem] §aOpen inventory delay set to: §e" + ticks + " ticks"));
                break;
            case "switchtotems":
                AutoInventoryTotem.setSwitchTotemsDelay(ticks);
                context.getSource().sendFeedback(Text.literal("§6[AutoTotem] §aSwitch totems delay set to: §e" + ticks + " ticks"));
                break;
            case "closeinventory":
                AutoInventoryTotem.setCloseInventoryDelay(ticks);
                context.getSource().sendFeedback(Text.literal("§6[AutoTotem] §aClose inventory delay set to: §e" + ticks + " ticks"));
                break;
        }

        return 1;
    }

    private static int setMode(com.mojang.brigadier.context.CommandContext<FabricClientCommandSource> context, String mode) {
        switch (mode.toLowerCase()) {
            case "fast" -> AutoInventoryTotem.setRefillMode(AutoInventoryTotem.Mode.FAST);
            case "semi" -> AutoInventoryTotem.setRefillMode(AutoInventoryTotem.Mode.SEMI);
            case "visible" -> AutoInventoryTotem.setRefillMode(AutoInventoryTotem.Mode.VISIBLE);
        }
        context.getSource().sendFeedback(Text.literal("§6[AutoTotem] §aMode set to: §e" + AutoInventoryTotem.getRefillMode().name()));
        return 1;
    }

    private static int toggleMod(com.mojang.brigadier.context.CommandContext<FabricClientCommandSource> context, boolean enable) {
        AutoInventoryTotem.setModEnabled(enable);
        context.getSource().sendFeedback(Text.literal("§6[AutoTotem] §aMod " + (enable ? "Enabled" : "Disabled")));
        return 1;
    }

    private static int showInfo(com.mojang.brigadier.context.CommandContext<FabricClientCommandSource> context) {
        int slot = AutoInventoryTotem.getConfiguredSlot() + 1;
        context.getSource().sendFeedback(Text.literal("§6§m-------------------§r §6[AutoTotem Info]§r §6§m-------------------"));
        context.getSource().sendFeedback(Text.literal("§eStatus: " + (AutoInventoryTotem.isModEnabled() ? "§aEnabled" : "§cDisabled")));
        context.getSource().sendFeedback(Text.literal("§eConfigured Slot: §b" + slot));
        context.getSource().sendFeedback(Text.literal("§eMode: §b" + AutoInventoryTotem.getRefillMode().name()));
        context.getSource().sendFeedback(Text.literal("§eDouble Hand Delay: §b" + AutoInventoryTotem.getDoubleHandDelay() + " ticks"));
        context.getSource().sendFeedback(Text.literal("§eOpen Inventory Delay: §b" + AutoInventoryTotem.getOpenInventoryDelay() + " ticks"));
        context.getSource().sendFeedback(Text.literal("§eSwitch Totems Delay: §b" + AutoInventoryTotem.getSwitchTotemsDelay() + " ticks"));
        context.getSource().sendFeedback(Text.literal("§eClose Inventory Delay: §b" + AutoInventoryTotem.getCloseInventoryDelay() + " ticks"));
        context.getSource().sendFeedback(Text.literal("§6§m-----------------------------------------------------"));

        return 1;
    }

    private static String getHelp() {
        return """
            §6/tt help §r- show this help
            §6/tt enable|disable §r- enable or disable the mod
            §6/tt mode <fast|semi|visible> §r- set refill GUI mode
            §6/tt slot <1-9> §r- set hotbar slot (1 = leftmost)
            §6/tt delay <doublehand|openinventory|switchtotems|closeinventory> <ticks> §r- set delays
            §6/tt gui §r- open settings GUI
            §6/tt info|status §r- show settings
            """;
    }
}
