package com.allianceering.network;

import com.allianceering.AllianceRingMod;
import com.allianceering.voicechat.AllianceVoicechatPlugin;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Payload sent from Client to Server when ending an active phone call.
 */
public record AllianceEndCallPayload(String reason) implements CustomPacketPayload {
    public static final Type<AllianceEndCallPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(AllianceRingMod.MODID, "end_call"));

    public static final StreamCodec<ByteBuf, AllianceEndCallPayload> STREAM_CODEC = ByteBufCodecs.STRING_UTF8.map(AllianceEndCallPayload::new, AllianceEndCallPayload::reason);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(AllianceEndCallPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;

            AllianceVoicechatPlugin.endCall(player.getUUID());
            player.sendSystemMessage(Component.literal("§c[Alliance Phone] Call ended."));
        });
    }
}
