package com.allianceering.network;

import com.allianceering.AllianceRingMod;
import com.allianceering.client.gui.AllianceRingScreen;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Payload sent from Server to Client when a call is accepted and connected.
 */
public record AllianceCallConnectedPayload(String partnerName) implements CustomPacketPayload {
    public static final Type<AllianceCallConnectedPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(AllianceRingMod.MODID, "call_connected"));

    public static final StreamCodec<ByteBuf, AllianceCallConnectedPayload> STREAM_CODEC = ByteBufCodecs.STRING_UTF8.map(AllianceCallConnectedPayload::new, AllianceCallConnectedPayload::partnerName);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(AllianceCallConnectedPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            AllianceRingScreen.onCallConnected(payload.partnerName());
        });
    }
}
