package com.allianceering.network;

import com.allianceering.AllianceRingMod;
import com.allianceering.voicechat.AllianceVoicechatPlugin;
import io.netty.buffer.ByteBuf;
import java.util.UUID;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Payload sent from Client to Server to accept and answer an incoming call.
 */
public record AllianceAnswerCallPayload(String callerName) implements CustomPacketPayload {
    public static final Type<AllianceAnswerCallPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(AllianceRingMod.MODID, "answer_call"));

    public static final StreamCodec<ByteBuf, AllianceAnswerCallPayload> STREAM_CODEC = ByteBufCodecs.STRING_UTF8.map(AllianceAnswerCallPayload::new, AllianceAnswerCallPayload::callerName);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(AllianceAnswerCallPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer target)) return;

            boolean success = AllianceVoicechatPlugin.answerCall(target, payload.callerName());
            if (success && target.server != null) {
                UUID callerUuid = AllianceVoicechatPlugin.ACTIVE_CALLS.get(target.getUUID());
                if (callerUuid != null) {
                    ServerPlayer caller = target.server.getPlayerList().getPlayer(callerUuid);
                    if (caller != null) {
                        PacketDistributor.sendToPlayer(caller, new AllianceCallConnectedPayload(target.getName().getString()));
                        PacketDistributor.sendToPlayer(target, new AllianceCallConnectedPayload(caller.getName().getString()));
                    }
                }
            }
        });
    }
}
