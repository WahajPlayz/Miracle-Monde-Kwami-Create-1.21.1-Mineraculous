package com.allianceering.network;

import com.allianceering.AllianceRingMod;
import com.allianceering.client.gui.AllianceRingScreen;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Payload sent from Server to Recipient Client when receiving a message in the Messages app.
 */
public record AllianceReceiveMessagePayload(String sender, String message, long timestamp) implements CustomPacketPayload {

    public static final Type<AllianceReceiveMessagePayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(AllianceRingMod.MODID, "receive_message"));

    public static final StreamCodec<ByteBuf, AllianceReceiveMessagePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, AllianceReceiveMessagePayload::sender,
            ByteBufCodecs.STRING_UTF8, AllianceReceiveMessagePayload::message,
            ByteBufCodecs.VAR_LONG, AllianceReceiveMessagePayload::timestamp,
            AllianceReceiveMessagePayload::new);
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(AllianceReceiveMessagePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            // Client-side execution
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null) {
                // 1. Add to conversation history
                AllianceRingScreen.receiveIncomingMessage(payload.sender(), payload.message(), payload.timestamp());

                // 2. Display chat notification with sound
                mc.player.displayClientMessage(
                        Component.literal("§a§l[Alliance Messages] §f" + payload.sender() + ": §e" + payload.message()),
                        false);

                // 3. Play incoming message chime
                mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.EXPERIENCE_ORB_PICKUP, 1.2f));
            }
        });
    }
}
