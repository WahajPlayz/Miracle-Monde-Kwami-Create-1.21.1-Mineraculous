package com.allianceering.network;

import com.allianceering.AllianceRingMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Payload sent from Client to Server when sending a message in the Messages app.
 * Delivers chat notifications to the recipient, updates GUI conversation history,
 * and optionally broadcasts to global server chat if enabled by a server admin!
 */
public record AllianceSendMessagePayload(String recipient, String message) implements CustomPacketPayload {
    public static final Type<AllianceSendMessagePayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(AllianceRingMod.MODID, "send_message"));

    public static final StreamCodec<ByteBuf, AllianceSendMessagePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, AllianceSendMessagePayload::recipient,
            ByteBufCodecs.STRING_UTF8, AllianceSendMessagePayload::message,
            AllianceSendMessagePayload::new);

    // Admin option: Whether in-ring messages are broadcast to global chat
    public static boolean broadcastGlobalMessages = false;

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(AllianceSendMessagePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer sender)) return;

            String recipientName = payload.recipient();
            String messageText = payload.message();
            String senderName = sender.getName().getString();

            if (sender.server == null) return;

            ServerPlayer target = null;
            for (ServerPlayer p : sender.server.getPlayerList().getPlayers()) {
                if (p.getName().getString().equalsIgnoreCase(recipientName)) {
                    target = p;
                    break;
                }
            }

            if (target != null && target.isAlive()) {
                // 1. Deliver message packet to target's client GUI conversation history
                PacketDistributor.sendToPlayer(target, new AllianceReceiveMessagePayload(senderName, messageText, System.currentTimeMillis()));

                // 2. Broadcast in-chat message notification to target player
                target.sendSystemMessage(Component.literal("§a§l[Alliance Messages] §f" + senderName + " §7» §e" + messageText));

                // 3. Play pleasant notification chime for recipient
                target.playNotifySound(SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1.0f, 1.2f);

                // 4. Send confirmation to sender
                sender.sendSystemMessage(Component.literal("§a[Alliance Messages] §7Sent to §f" + target.getName().getString() + ": §e" + messageText));

                // 5. Server Admin Option: Global broadcast if enabled
                if (broadcastGlobalMessages) {
                    Component globalMsg = Component.literal("§6[Alliance] §f" + senderName + " §7-> §f" + target.getName().getString() + ": §e" + messageText);
                    sender.server.getPlayerList().broadcastSystemMessage(globalMsg, false);
                }
            } else {
                sender.sendSystemMessage(Component.literal("§c[Alliance Messages] Player '" + recipientName + "' is currently offline."));
            }
        });
    }
}
