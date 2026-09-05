package com.allianceering.command;

import com.allianceering.network.AllianceCallConnectedPayload;
import com.allianceering.network.AllianceSendMessagePayload;
import com.allianceering.voicechat.AllianceVoicechatPlugin;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import java.util.UUID;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

public class AllianceCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("alliance")
                // /alliance answer <caller>
                .then(Commands.literal("answer")
                        .then(Commands.argument("caller", StringArgumentType.string())
                                .executes(ctx -> {
                                    CommandSourceStack source = ctx.getSource();
                                    ServerPlayer player = source.getPlayerOrException();
                                    String callerName = StringArgumentType.getString(ctx, "caller");

                                    boolean success = AllianceVoicechatPlugin.answerCall(player, callerName);
                                    if (success && player.server != null) {
                                        UUID callerUuid = AllianceVoicechatPlugin.ACTIVE_CALLS.get(player.getUUID());
                                        if (callerUuid != null) {
                                            ServerPlayer caller = player.server.getPlayerList().getPlayer(callerUuid);
                                            if (caller != null) {
                                                PacketDistributor.sendToPlayer(caller, new AllianceCallConnectedPayload(player.getName().getString()));
                                                PacketDistributor.sendToPlayer(player, new AllianceCallConnectedPayload(caller.getName().getString()));
                                            }
                                        }
                                    }
                                    return 1;
                                }))
                        .executes(ctx -> {
                            CommandSourceStack source = ctx.getSource();
                            ServerPlayer player = source.getPlayerOrException();

                            UUID callerUuid = AllianceVoicechatPlugin.getPendingCaller(player.getUUID());
                            if (callerUuid == null || player.server == null) {
                                player.sendSystemMessage(Component.literal("§c[Alliance Phone] No incoming call found."));
                                return 0;
                            }
                            ServerPlayer caller = player.server.getPlayerList().getPlayer(callerUuid);
                            String callerName = (caller != null) ? caller.getName().getString() : "";

                            boolean success = AllianceVoicechatPlugin.answerCall(player, callerName);
                            if (success && caller != null) {
                                PacketDistributor.sendToPlayer(caller, new AllianceCallConnectedPayload(player.getName().getString()));
                                PacketDistributor.sendToPlayer(player, new AllianceCallConnectedPayload(caller.getName().getString()));
                            }
                            return 1;
                        }))
                // /alliance messages global <true|false> (Server Admin Option)
                .then(Commands.literal("messages")
                        .then(Commands.literal("global")
                                .requires(source -> source.hasPermission(2))
                                .then(Commands.argument("enabled", BoolArgumentType.bool())
                                        .executes(ctx -> {
                                            boolean enabled = BoolArgumentType.getBool(ctx, "enabled");
                                            AllianceSendMessagePayload.broadcastGlobalMessages = enabled;
                                            ctx.getSource().sendSuccess(() -> Component.literal("§a[Alliance Messages] Global broadcast is now: " + (enabled ? "§2ENABLED" : "§cDISABLED")), true);
                                            return 1;
                                        }))
                                .executes(ctx -> {
                                    boolean cur = AllianceSendMessagePayload.broadcastGlobalMessages;
                                    ctx.getSource().sendSuccess(() -> Component.literal("§e[Alliance Messages] Global broadcast is currently: " + (cur ? "§2ENABLED" : "§cDISABLED")), false);
                                    return 1;
                                }))));
    }
}
