package com.allianceering.network;

import com.allianceering.AllianceRingMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Payload sent from Server to Target Player Client when receiving an incoming call.
 */
public record AllianceIncomingCallPayload(String caller) implements CustomPacketPayload {
    public static final Type<AllianceIncomingCallPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(AllianceRingMod.MODID, "incoming_call"));

    public static final StreamCodec<ByteBuf, AllianceIncomingCallPayload> STREAM_CODEC = ByteBufCodecs.STRING_UTF8.map(AllianceIncomingCallPayload::new, AllianceIncomingCallPayload::caller);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(AllianceIncomingCallPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null) {
                // 1. Notify client screen of incoming call
                com.allianceering.client.gui.AllianceRingScreen.onIncomingCall(payload.caller());

                // 2. Play telephone ringing bell
                mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.NOTE_BLOCK_BELL.value(), 1.0f));
            }
        });
    }
}
