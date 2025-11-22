package com.autototem;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;

public class TotemCommand {
    
    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            registerCommand(dispatcher);
        });
    }
    
    private static void registerCommand(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(ClientCommandManager.literal("tt")
            .then(ClientCommandManager.literal("slot")
                .then(ClientCommandManager.argument("slotNumber", IntegerArgumentType.integer(1, 9))
                    .executes(TotemCommand::setSlot)))
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
            .then(ClientCommandManager.literal("info")
                .executes(TotemCommand::showInfo))
        );
    }
    
    private static int setSlot(CommandContext<FabricClientCommandSource> context) {
        int slotNumber = IntegerArgumentType.getInteger(context, "slotNumber");
        int slotIndex = slotNumber - 1; // Convert to 0-indexed
        
        AutoInventoryTotem.setConfiguredSlot(slotIndex);
        context.getSource().sendFeedback(
            Text.literal("§6[AutoTotem] §aConfigured hotbar slot set to: §e" + slotNumber)
        );
        
        return 1;
    }
    
    private static int setDelay(CommandContext<FabricClientCommandSource> context, String delayType) {
        int ticks = IntegerArgumentType.getInteger(context, "ticks");
        
        switch (delayType) {
            case "doublehand":
                AutoInventoryTotem.setDoubleHandDelay(ticks);
                context.getSource().sendFeedback(
                    Text.literal("§6[AutoTotem] §aDouble hand delay set to: §e" + ticks + " ticks")
                );
                break;
            case "openinventory":
                AutoInventoryTotem.setOpenInventoryDelay(ticks);
                context.getSource().sendFeedback(
                    Text.literal("§6[AutoTotem] §aOpen inventory delay set to: §e" + ticks + " ticks")
                );
                break;
            case "switchtotems":
                AutoInventoryTotem.setSwitchTotemsDelay(ticks);
                context.getSource().sendFeedback(
                    Text.literal("§6[AutoTotem] §aSwitch totems delay set to: §e" + ticks + " ticks")
                );
                break;
            case "closeinventory":
                AutoInventoryTotem.setCloseInventoryDelay(ticks);
                context.getSource().sendFeedback(
                    Text.literal("§6[AutoTotem] §aClose inventory delay set to: §e" + ticks + " ticks")
                );
                break;
        }
        
        return 1;
    }
    
    private static int showInfo(CommandContext<FabricClientCommandSource> context) {
        int slot = AutoInventoryTotem.getConfiguredSlot() + 1;
        context.getSource().sendFeedback(Text.literal("§6§m-------------------§r §6[AutoTotem Info]§r §6§m-------------------"));
        context.getSource().sendFeedback(Text.literal("§eStatus: " + (AutoInventoryTotem.isModEnabled() ? "§aEnabled" : "§cDisabled")));
        context.getSource().sendFeedback(Text.literal("§eConfigured Slot: §b" + slot));
        context.getSource().sendFeedback(Text.literal("§eDouble Hand Delay: §b" + AutoInventoryTotem.getDoubleHandDelay() + " ticks"));
        context.getSource().sendFeedback(Text.literal("§eOpen Inventory Delay: §b" + AutoInventoryTotem.getOpenInventoryDelay() + " ticks"));
        context.getSource().sendFeedback(Text.literal("§eSwitch Totems Delay: §b" + AutoInventoryTotem.getSwitchTotemsDelay() + " ticks"));
        context.getSource().sendFeedback(Text.literal("§eClose Inventory Delay: §b" + AutoInventoryTotem.getCloseInventoryDelay() + " ticks"));
        context.getSource().sendFeedback(Text.literal("§6§m-----------------------------------------------------"));
        
        return 1;
    }
}
