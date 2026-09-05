package com.allianceering.voicechat;

import com.allianceering.AllianceRingMod;
import de.maxhenkel.voicechat.api.ForgeVoicechatPlugin;
import de.maxhenkel.voicechat.api.VoicechatConnection;
import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.api.VoicechatServerApi;
import de.maxhenkel.voicechat.api.events.EventRegistration;
import de.maxhenkel.voicechat.api.events.MicrophonePacketEvent;
import de.maxhenkel.voicechat.api.events.VoicechatServerStartedEvent;
import de.maxhenkel.voicechat.api.events.VoicechatServerStoppedEvent;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;

/**
 * Simple Voice Chat Plugin for Alliance Ring.
 * Directly routes microphone audio between caller and recipient via Simple Voice Chat's
 * static sound packet API (sendStaticSoundPacketTo) WITHOUT creating or switching groups,
 * preserving any existing groups the players are already in!
 */
@ForgeVoicechatPlugin
public class AllianceVoicechatPlugin implements VoicechatPlugin {
    private static VoicechatServerApi serverApi;

    // Active calls: player UUID <-> partner UUID
    public static final Map<UUID, UUID> ACTIVE_CALLS = new ConcurrentHashMap<>();

    // Pending incoming calls: target UUID -> caller UUID
    public static final Map<UUID, UUID> PENDING_CALLS = new ConcurrentHashMap<>();

    @Override
    public String getPluginId() {
        return AllianceRingMod.MODID;
    }

    @Override
    public void registerEvents(EventRegistration registration) {
        registration.registerEvent(VoicechatServerStartedEvent.class, this::onServerStarted);
        registration.registerEvent(VoicechatServerStoppedEvent.class, this::onServerStopped);
        registration.registerEvent(MicrophonePacketEvent.class, this::onMicrophone);
    }

    private void onServerStarted(VoicechatServerStartedEvent event) {
        serverApi = event.getVoicechat();
        AllianceRingMod.LOGGER.info("Alliance Ring: Simple Voice Chat direct audio routing enabled!");
    }

    private void onServerStopped(VoicechatServerStoppedEvent event) {
        serverApi = null;
        ACTIVE_CALLS.clear();
        PENDING_CALLS.clear();
    }

    /**
     * Intercepts microphone packets and directly routes them to the active phone call partner.
     * ZERO groups are modified, so players stay in their existing groups!
     */
    private void onMicrophone(MicrophonePacketEvent event) {
        if (serverApi == null) return;

        VoicechatConnection senderConn = event.getSenderConnection();
        if (senderConn == null || senderConn.getPlayer() == null) return;

        UUID senderUuid = senderConn.getPlayer().getUuid();
        UUID partnerUuid = ACTIVE_CALLS.get(senderUuid);
        if (partnerUuid == null) return;

        try {
            VoicechatConnection receiverConn = serverApi.getConnectionOf(partnerUuid);
            if (receiverConn != null && receiverConn.isConnected()) {
                // Transmit audio directly to the other player's headset as static sound!
                serverApi.sendStaticSoundPacketTo(receiverConn, event.getPacket().toStaticSoundPacket());
            }
        } catch (Exception e) {
            // Ignore temporary transmission errors
        }
    }

    public static VoicechatServerApi getServerApi() {
        return serverApi;
    }

    public static boolean isVoiceChatAvailable() {
        return serverApi != null;
    }

    public static boolean isPlayerConnected(UUID playerUuid) {
        if (serverApi == null) return false;
        try {
            VoicechatConnection conn = serverApi.getConnectionOf(playerUuid);
            return conn != null && conn.isConnected();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Checks if a player has an Alliance Ring anywhere in their hotbar (slots 0..8 or offhand).
     */
    public static boolean hasAllianceRingInHotbar(Player player) {
        if (player.getOffhandItem().is(AllianceRingMod.ALLIANCE_RING.get())) return true;
        for (int i = 0; i < 9; i++) {
            if (player.getInventory().getItem(i).is(AllianceRingMod.ALLIANCE_RING.get())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Initiates a pending call from caller to target.
     * Does NOT connect audio until target accepts!
     */
    public static void requestCall(ServerPlayer caller, ServerPlayer target) {
        PENDING_CALLS.put(target.getUUID(), caller.getUUID());

        // Play ringing sound to target
        target.playNotifySound(SoundEvents.NOTE_BLOCK_BELL.value(), SoundSource.PLAYERS, 1.0f, 1.0f);

        // Clickable chat message to answer
        Component answerBtn = Component.literal(" [📞 CLICK TO ANSWER]")
                .withStyle(style -> style
                        .withColor(0x55FF55)
                        .withBold(true)
                        .withUnderlined(true)
                        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/alliance answer " + caller.getName().getString()))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Click to answer phone call\n§7(Requires Alliance Ring in hotbar)"))));

        Component incomingMsg = Component.literal("§b§l[Alliance Phone] §eIncoming call from §f" + caller.getName().getString() + "§e!")
                .append(answerBtn);

        target.sendSystemMessage(incomingMsg);
        caller.sendSystemMessage(Component.literal("§a§l[Alliance Phone] §eCalling §f" + target.getName().getString() + "§e... (Waiting for answer)"));
    }

    /**
     * Answers a pending call. Verifies hotbar requirement before connecting audio!
     */
    public static boolean answerCall(ServerPlayer target, String callerName) {
        // 1. Verify hotbar requirement
        if (!hasAllianceRingInHotbar(target)) {
            target.sendSystemMessage(Component.literal("§c[Alliance Phone] You need an Alliance Ring in your hotbar to answer the call!"));
            return false;
        }

        UUID callerUuid = PENDING_CALLS.get(target.getUUID());
        if (callerUuid == null) {
            target.sendSystemMessage(Component.literal("§c[Alliance Phone] No incoming call found."));
            return false;
        }

        if (target.server == null) return false;
        ServerPlayer caller = target.server.getPlayerList().getPlayer(callerUuid);
        if (caller == null || !caller.isAlive()) {
            PENDING_CALLS.remove(target.getUUID());
            target.sendSystemMessage(Component.literal("§c[Alliance Phone] Caller is no longer online."));
            return false;
        }

        // Verify Voice Chat connection for both
        if (!isPlayerConnected(caller.getUUID()) || !isPlayerConnected(target.getUUID())) {
            target.sendSystemMessage(Component.literal("§c[Alliance Phone] Both players must be connected to Simple Voice Chat!"));
            return false;
        }

        // 2. Connect the call!
        PENDING_CALLS.remove(target.getUUID());
        ACTIVE_CALLS.put(caller.getUUID(), target.getUUID());
        ACTIVE_CALLS.put(target.getUUID(), caller.getUUID());

        // Notifications
        caller.sendSystemMessage(Component.literal("§a§l[Alliance Phone] §2Call connected with §f" + target.getName().getString() + "§2!"));
        target.sendSystemMessage(Component.literal("§a§l[Alliance Phone] §2Call connected with §f" + caller.getName().getString() + "§2!"));

        caller.playNotifySound(SoundEvents.NOTE_BLOCK_CHIME.value(), SoundSource.PLAYERS, 1.0f, 1.2f);
        target.playNotifySound(SoundEvents.NOTE_BLOCK_CHIME.value(), SoundSource.PLAYERS, 1.0f, 1.2f);

        return true;
    }

    /**
     * Ends an active call for a player and their partner.
     */
    public static void endCall(UUID playerUuid) {
        PENDING_CALLS.remove(playerUuid);

        UUID partnerUuid = ACTIVE_CALLS.remove(playerUuid);
        if (partnerUuid != null) {
            ACTIVE_CALLS.remove(partnerUuid);
        }
    }

    public static boolean hasPendingCall(UUID playerUuid) {
        return PENDING_CALLS.containsKey(playerUuid);
    }

    public static UUID getPendingCaller(UUID playerUuid) {
        return PENDING_CALLS.get(playerUuid);
    }
}
