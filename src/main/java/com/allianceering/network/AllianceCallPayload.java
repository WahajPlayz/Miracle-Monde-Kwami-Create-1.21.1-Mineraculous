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
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Payload sent from Client to Server when initiating a phone call with another player.
 * Enforces Simple Voice Chat and sends an incoming call invitation that requires the target to answer!
 */
public record AllianceCallPayload(String targetPlayer) implements CustomPacketPayload {
    public static final Type<AllianceCallPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(AllianceRingMod.MODID, "call_player"));

    public static final StreamCodec<ByteBuf, AllianceCallPayload> STREAM_CODEC = ByteBufCodecs.STRING_UTF8.map(AllianceCallPayload::new, AllianceCallPayload::targetPlayer);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(AllianceCallPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer caller)) return;

            String targetName = payload.targetPlayer();
            String callerName = caller.getName().getString();

            if (caller.server == null) return;

            // 1. Check if Simple Voice Chat is loaded
            if (!ModList.get().isLoaded("voicechat")) {
                caller.sendSystemMessage(Component.literal("§c[Alliance Phone] Calls require Simple Voice Chat! Please install the 'voicechat' mod."));
                return;
            }

            // 2. Check if Voice Chat server API is ready
            if (!AllianceVoicechatPlugin.isVoiceChatAvailable()) {
                caller.sendSystemMessage(Component.literal("§c[Alliance Phone] Simple Voice Chat server is not ready!"));
                return;
            }

            // 3. Check if caller is connected to Voice Chat
            if (!AllianceVoicechatPlugin.isPlayerConnected(caller.getUUID())) {
                caller.sendSystemMessage(Component.literal("§c[Alliance Phone] You must be connected to Simple Voice Chat to make calls!"));
                return;
            }

            // 4. Find target player
            ServerPlayer target = null;
            for (ServerPlayer p : caller.server.getPlayerList().getPlayers()) {
                if (p.getName().getString().equalsIgnoreCase(targetName)) {
                    target = p;
                    break;
                }
            }

            if (target == null || !target.isAlive()) {
                caller.sendSystemMessage(Component.literal("§c[Alliance Phone] Player '" + targetName + "' is offline."));
                return;
            }

            // 5. Check if target is connected to Voice Chat
            if (!AllianceVoicechatPlugin.isPlayerConnected(target.getUUID())) {
                caller.sendSystemMessage(Component.literal("§c[Alliance Phone] " + target.getName().getString() + " is not connected to Simple Voice Chat!"));
                return;
            }

            // 6. Request call: target must answer before voice connects!
            AllianceVoicechatPlugin.requestCall(caller, target);
            PacketDistributor.sendToPlayer(target, new AllianceIncomingCallPayload(callerName));
        });
    }
}
