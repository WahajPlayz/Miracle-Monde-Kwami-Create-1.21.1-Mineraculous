package com.allianceering.client.gui;

import com.allianceering.AllianceRingMod;
import com.allianceering.network.AllianceAnswerCallPayload;
import com.allianceering.network.AllianceCallPayload;
import com.allianceering.network.AllianceEndCallPayload;
import com.allianceering.network.AllianceSendMessagePayload;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.neoforged.neoforge.network.PacketDistributor;

import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Alliance Ring GUI.
 * - Perfect 1:1 white suit overlay tailored to the skin body without covering head/chin.
 * - Direct Voice Chat audio integration.
 * - Clickable chat call acceptance & hotbar check.
 * - Right-click ring to auto-answer.
 * - Clean borders and centered scaling for all window sizes.
 * - Messages app with real-time delivery and server admin global option.
 */
public class AllianceRingScreen extends Screen {
    private static final ResourceLocation BACKGROUND_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(AllianceRingMod.MODID, "textures/gui/alliance_ring_background.png");

    private static final ResourceLocation ADRIAN_SKIN =
            ResourceLocation.fromNamespaceAndPath(AllianceRingMod.MODID, "textures/gui/adrian_skin.png");
    private static final ResourceLocation KAGAMI_SKIN =
            ResourceLocation.fromNamespaceAndPath(AllianceRingMod.MODID, "textures/gui/kagami_skin.png");
    private static final ResourceLocation LILA_SKIN =
            ResourceLocation.fromNamespaceAndPath(AllianceRingMod.MODID, "textures/gui/lila_skin.png");

    private static final ResourceLocation WHITE_SUIT_WIDE =
            ResourceLocation.fromNamespaceAndPath(AllianceRingMod.MODID, "textures/gui/white_suit_wide.png");
    private static final ResourceLocation WHITE_SUIT_SLIM =
            ResourceLocation.fromNamespaceAndPath(AllianceRingMod.MODID, "textures/gui/white_suit_slim.png");

    // Bottom dock icons
    private static final ResourceLocation ICON_HOME =
            ResourceLocation.fromNamespaceAndPath(AllianceRingMod.MODID, "textures/gui/icon_home.png");
    private static final ResourceLocation ICON_CUSTOMIZATION =
            ResourceLocation.fromNamespaceAndPath(AllianceRingMod.MODID, "textures/gui/icon_customization.png");
    private static final ResourceLocation ICON_MESSAGING =
            ResourceLocation.fromNamespaceAndPath(AllianceRingMod.MODID, "textures/gui/icon_messaging.png");
    private static final ResourceLocation ICON_CALLING =
            ResourceLocation.fromNamespaceAndPath(AllianceRingMod.MODID, "textures/gui/icon_calling.png");
    private static final ResourceLocation ICON_SOS =
            ResourceLocation.fromNamespaceAndPath(AllianceRingMod.MODID, "textures/gui/icon_sos.png");

    // Customization panel corner icons
    private static final ResourceLocation ICON_SELECT_SKIN =
            ResourceLocation.fromNamespaceAndPath(AllianceRingMod.MODID, "textures/gui/icon_select_skin.png");
    private static final ResourceLocation ICON_CUSTOM_SKIN =
            ResourceLocation.fromNamespaceAndPath(AllianceRingMod.MODID, "textures/gui/icon_custom_skin.png");
    private static final ResourceLocation ICON_SHOP =
            ResourceLocation.fromNamespaceAndPath(AllianceRingMod.MODID, "textures/gui/icon_shop.png");
    private static final ResourceLocation ICON_MODEL_TOGGLE =
            ResourceLocation.fromNamespaceAndPath(AllianceRingMod.MODID, "textures/gui/icon_model_toggle.png");

    // Dimensions
    private static final int PANEL_WIDTH = 100;
    private static final int PANEL_HEIGHT = 150;
    private static final int TOTAL_GUI_HEIGHT = 224;

    // Center card dimensions for avatar (32x64 for exact 2:1 ratio and sharp 2x integer Minecraft pixel scaling)
    private static final int CENTER_WIDTH = 32;
    private static final int CENTER_HEIGHT = 64;
    private static final int CAROUSEL_CENTER_Y_OFFSET = 48;

    private int leftPos;
    private int topPos;

    // View Modes
    public enum ViewMode {
        MAIN,                   // Home Screen
        CUSTOMIZATION,          // Wardrobe Hub
        SELECT_SKIN,            // 3-Avatar Carousel
        CUSTOM_SKIN_INPUT,      // Custom Skin Fetcher
        SHOP,                   // Placeholders Shop
        MESSAGING_CONTACTS,     // Messages Player Selector
        MESSAGING_CHAT,         // Messages Direct Chat View
        CALLING_CONTACTS,       // Calling Player Selector
        CALLING_ACTIVE,         // Active Call Screen
        CALLING_INCOMING,       // Incoming Call Prompt
        SOS                     // Emergency Alert Screen
    }
    private ViewMode currentMode = ViewMode.MAIN;

    // STATIC PERSISTENCE
    private static int selectedCharacterIndex = 0;
    private static String activeChatContact = "";
    private static String activeCallContact = "";
    private static boolean isCallConnected = false;
    private static long callStartTime = 0;
    private static String pendingIncomingCaller = "";

    // Message Data Structure
    public record ChatBubble(String sender, String text, boolean isSelf, long timestamp) {}
    private static final Map<String, List<ChatBubble>> CONVERSATIONS = new LinkedHashMap<>();

    private float currentScrollPos = 0.0f;
    private int targetScrollIndex = 0;

    // Bottom Navigation Icon Buttons
    private RingIconButton homeDockButton;
    private RingIconButton customDockButton;
    private RingIconButton messagingDockButton;
    private RingIconButton callingDockButton;
    private RingIconButton sosDockButton;

    // Customization Hub Buttons (icon buttons in top-left corner)
    private RingIconButton customSelectSkinBtn;
    private RingIconButton customCustomSkinBtn;
    private RingIconButton customShopBtn;
    private RingIconButton suitToggleButton;

    // Shop View Buttons
    private Button shopBackButton;

    // Select Skin View Buttons
    private Button applyCostumeButton;
    private Button skinBackButton;

    // Custom Skin Input Elements
    private EditBox skinInputBox;
    private Button applyCustomSkinButton;
    private Button customInputBackButton;

    // Messages Chat Elements
    private EditBox chatInputBox;
    private Button chatSendButton;
    private Button chatBackToContactsBtn;

    // Calling Elements
    private Button endCallButton;
    private Button callBackToContactsBtn;
    private Button answerCallButton;
    private Button declineCallButton;

    // SOS Buttons
    private Button akumaAlertButton;
    private Button sosHelpButton;

    // Cache raw 64x64 skins for dynamic re-generation between Slim & Wide
    private static final Map<String, NativeImage> CUSTOM_RAW_SKINS = new HashMap<>();

    public record CharacterEntry(String name, ResourceLocation texture, boolean isSlim) {}
    private static final List<CharacterEntry> CHARACTERS = new ArrayList<>(List.of(
            new CharacterEntry("ADRIAN", ADRIAN_SKIN, false),
            new CharacterEntry("KAGAMI", KAGAMI_SKIN, true),
            new CharacterEntry("LILA", LILA_SKIN, true)
    ));

    public AllianceRingScreen() {
        super(Component.translatable("gui.allianceering.alliance_ring.title"));
    }

    public static void onIncomingCall(String caller) {
        pendingIncomingCaller = caller;
    }

    public static void onCallConnected(String partner) {
        activeCallContact = partner;
        isCallConnected = true;
        callStartTime = System.currentTimeMillis();
        pendingIncomingCaller = "";
    }

    public static boolean hasPendingIncomingCall() {
        return pendingIncomingCaller != null && !pendingIncomingCaller.isEmpty();
    }

    public static void answerPendingCall() {
        if (hasPendingIncomingCall()) {
            PacketDistributor.sendToServer(new AllianceAnswerCallPayload(pendingIncomingCaller));
            activeCallContact = pendingIncomingCaller;
            isCallConnected = true;
            callStartTime = System.currentTimeMillis();
            pendingIncomingCaller = "";
        }
    }

    private float calculateGuiScale() {
        float availableW = Math.max(100.0f, this.width - 16.0f);
        float availableH = Math.max(150.0f, this.height - 16.0f);
        float totalW = PANEL_WIDTH + 48.0f; // Account for outside left tabs
        float scaleX = availableW / totalW;
        float scaleY = availableH / TOTAL_GUI_HEIGHT;
        return Math.min(1.0f, Math.min(scaleX, scaleY));
    }

    public class RingIconButton extends Button {
        private final ResourceLocation icon;
        private final int iconSize;
        private final boolean isTabButton;

        public RingIconButton(int x, int y, int width, int height, ResourceLocation icon, int iconSize, Component tooltip, OnPress onPress) {
            this(x, y, width, height, icon, iconSize, tooltip, onPress, false);
        }

        public RingIconButton(int x, int y, int width, int height, ResourceLocation icon, int iconSize, Component tooltip, OnPress onPress, boolean isTabButton) {
            super(x, y, width, height, Component.empty(), onPress, DEFAULT_NARRATION);
            this.icon = icon;
            this.iconSize = iconSize;
            this.isTabButton = isTabButton;
            this.setTooltip(Tooltip.create(tooltip));
        }

        @Override
        public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            boolean hovered = this.isHoveredOrFocused();

            if (this.isTabButton) {
                // Tab styling matching image 4: clean square tab attached to left edge of GUI
                int bg = hovered ? 0xFFFFFFFF : 0xFFE2E8F0;
                int border = hovered ? 0xFF38BDF8 : 0xFF64748B;
                graphics.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, bg);
                graphics.renderOutline(this.getX(), this.getY(), this.width, this.height, border);

                // Render clean question mark '?' centered instead of icons
                int textColor = hovered ? 0xFF0284C7 : 0xFF475569;
                var font = Minecraft.getInstance().font;
                String qMark = "?";
                int qW = font.width(qMark);
                int qX = this.getX() + (this.width - qW) / 2;
                int qY = this.getY() + (this.height - 8) / 2;
                graphics.drawString(font, qMark, qX, qY, textColor, false);
            } else {
                if (hovered) {
                    graphics.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, 0x30FFFFFF);
                }

                int iconX = this.getX() + (this.width - this.iconSize) / 2;
                int iconY = this.getY() + (this.height - this.iconSize) / 2;
                graphics.blit(this.icon, iconX, iconY, 0.0f, 0.0f, this.iconSize, this.iconSize, this.iconSize, this.iconSize);
            }
        }
    }

    @Override
    protected void init() {
        super.init();

        this.leftPos = (this.width - PANEL_WIDTH) / 2;
        this.topPos = (this.height - TOTAL_GUI_HEIGHT) / 2;
        int centerX = this.leftPos + PANEL_WIDTH / 2;

        // If an incoming call is waiting, open directly to CALLING_INCOMING (do not auto-answer!)
        if (hasPendingIncomingCall()) {
            currentMode = ViewMode.CALLING_INCOMING;
        } else if (isCallConnected) {
            currentMode = ViewMode.CALLING_ACTIVE;
        }

        // ==========================================
        // 1. BOTTOM DOCK
        // ==========================================
        int row1Y = this.topPos + PANEL_HEIGHT + 6;
        int row2Y = this.topPos + PANEL_HEIGHT + 25;
        int row3Y = this.topPos + PANEL_HEIGHT + 48;

        this.callingDockButton = new RingIconButton(
                centerX - 30, row1Y + 2, 16, 16, ICON_CALLING, 14,
                Component.translatable("gui.allianceering.alliance_ring.calling.tooltip"),
                btn -> setViewMode(isCallConnected ? ViewMode.CALLING_ACTIVE : ViewMode.CALLING_CONTACTS)
        );

        this.customDockButton = new RingIconButton(
                centerX - 8, row1Y, 16, 16, ICON_HOME, 14,
                Component.translatable("gui.allianceering.alliance_ring.customization.tooltip"),
                btn -> setViewMode(ViewMode.CUSTOMIZATION)
        );

        this.messagingDockButton = new RingIconButton(
                centerX + 14, row1Y + 2, 16, 16, ICON_MESSAGING, 14,
                Component.translatable("gui.allianceering.alliance_ring.messaging.tooltip"),
                btn -> setViewMode(ViewMode.MESSAGING_CONTACTS)
        );

        this.homeDockButton = new RingIconButton(
                centerX - 10, row2Y, 20, 20, ICON_CUSTOMIZATION, 18,
                Component.translatable("gui.allianceering.alliance_ring.home.tooltip"),
                btn -> setViewMode(ViewMode.MAIN)
        );

        this.sosDockButton = new RingIconButton(
                centerX - 16, row3Y, 32, 32, ICON_SOS, 30,
                Component.translatable("gui.allianceering.alliance_ring.sos.tooltip"),
                btn -> setViewMode(ViewMode.SOS)
        );

        // ==========================================
        // 2. CUSTOMIZATION HUB TABS — outside left edge of the GUI panel (matching image 4)
        // ==========================================
        int tabW = 22;
        int tabH = 22;
        int tabX = this.leftPos - tabW;
        int tabStartY = this.topPos + 8;
        int tabGap = 3;

        this.customSelectSkinBtn = new RingIconButton(
                tabX, tabStartY,
                tabW, tabH,
                ICON_SELECT_SKIN, 16,
                Component.translatable("gui.allianceering.alliance_ring.select_skin"),
                btn -> setViewMode(ViewMode.SELECT_SKIN),
                true);

        this.customCustomSkinBtn = new RingIconButton(
                tabX, tabStartY + (tabH + tabGap),
                tabW, tabH,
                ICON_CUSTOM_SKIN, 16,
                Component.translatable("gui.allianceering.alliance_ring.custom_skin"),
                btn -> setViewMode(ViewMode.CUSTOM_SKIN_INPUT),
                true);

        this.customShopBtn = new RingIconButton(
                tabX, tabStartY + 2 * (tabH + tabGap),
                tabW, tabH,
                ICON_SHOP, 16,
                Component.literal("Shop"),
                btn -> setViewMode(ViewMode.SHOP),
                true);

        this.suitToggleButton = new RingIconButton(
                tabX, tabStartY + 3 * (tabH + tabGap),
                tabW, tabH,
                ICON_MODEL_TOGGLE, 16,
                Component.literal("Toggle Wide / Slim"),
                btn -> toggleSelectedCharacterModel(),
                true);

        // Shop Back Button
        this.shopBackButton = Button.builder(
                        Component.translatable("gui.allianceering.alliance_ring.back"),
                        btn -> setViewMode(ViewMode.CUSTOMIZATION))
                .bounds(centerX - 24, this.topPos + 124, 48, 16)
                .build();

        // ==========================================
        // 3. SELECT SKIN VIEW BUTTONS
        // ==========================================
        this.applyCostumeButton = Button.builder(
                        Component.literal("Equip"),
                        btn -> confirmSelectedSkin())
                .bounds(centerX - 46, this.topPos + 114, 44, 16)
                .build();

        this.skinBackButton = Button.builder(
                        Component.translatable("gui.allianceering.alliance_ring.back"),
                        btn -> setViewMode(ViewMode.CUSTOMIZATION))
                .bounds(centerX + 2, this.topPos + 114, 44, 16)
                .build();

        // ==========================================
        // 4. CUSTOM SKIN INPUT
        // ==========================================
        this.skinInputBox = new EditBox(this.font, centerX - 48, this.topPos + 84, 96, 14, Component.literal("Skin Input"));
        this.skinInputBox.setMaxLength(256);
        this.skinInputBox.setHint(Component.literal("Username / URL..."));

        this.applyCustomSkinButton = Button.builder(
                        Component.translatable("gui.allianceering.alliance_ring.apply"),
                        btn -> fetchCustomSkin(this.skinInputBox.getValue()))
                .bounds(centerX - 46, this.topPos + 110, 44, 16)
                .build();

        this.customInputBackButton = Button.builder(
                        Component.translatable("gui.allianceering.alliance_ring.back"),
                        btn -> setViewMode(ViewMode.CUSTOMIZATION))
                .bounds(centerX + 2, this.topPos + 110, 44, 16)
                .build();

        // ==========================================
        // 5. MESSAGES CHAT VIEW ELEMENTS
        // ==========================================
        this.chatBackToContactsBtn = Button.builder(
                        Component.literal("◀"),
                        btn -> setViewMode(ViewMode.MESSAGING_CONTACTS))
                .tooltip(Tooltip.create(Component.literal("Back to Contacts")))
                .bounds(this.leftPos + 4, this.topPos + 6, 14, 12)
                .build();

        this.chatInputBox = new EditBox(this.font, this.leftPos + 4, this.topPos + 128, PANEL_WIDTH - 24, 14, Component.literal("Chat Input"));
        this.chatInputBox.setMaxLength(128);
        this.chatInputBox.setHint(Component.literal("Message..."));

        this.chatSendButton = Button.builder(
                        Component.literal("▶"),
                        btn -> sendMessage())
                .tooltip(Tooltip.create(Component.literal("Send Message")))
                .bounds(this.leftPos + PANEL_WIDTH - 18, this.topPos + 128, 14, 14)
                .build();

        // ==========================================
        // 6. CALLING VIEW ELEMENTS
        // ==========================================
        this.callBackToContactsBtn = Button.builder(
                        Component.literal("◀"),
                        btn -> setViewMode(ViewMode.CALLING_CONTACTS))
                .tooltip(Tooltip.create(Component.literal("Back to Contacts")))
                .bounds(this.leftPos + 4, this.topPos + 6, 14, 12)
                .build();

        this.endCallButton = Button.builder(
                        Component.literal("🔴 End Call"),
                        btn -> endCurrentCall())
                .tooltip(Tooltip.create(Component.literal("Hang up")))
                .bounds(centerX - 35, this.topPos + 118, 70, 16)
                .build();

        this.answerCallButton = Button.builder(
                        Component.literal("📞 Accept"),
                        btn -> {
                            answerPendingCall();
                            setViewMode(ViewMode.CALLING_ACTIVE);
                        })
                .tooltip(Tooltip.create(Component.literal("Accept the call")))
                .bounds(centerX - 44, this.topPos + 80, 42, 22)
                .build();

        this.declineCallButton = Button.builder(
                        Component.literal("❌ Decline"),
                        btn -> {
                            pendingIncomingCaller = "";
                            PacketDistributor.sendToServer(new AllianceEndCallPayload("decline"));
                            setViewMode(ViewMode.MAIN);
                        })
                .tooltip(Tooltip.create(Component.literal("Decline call")))
                .bounds(centerX + 2, this.topPos + 80, 42, 22)
                .build();

        // ==========================================
        // 7. SOS / EMERGENCY ALERT BUTTONS
        // ==========================================
        this.akumaAlertButton = Button.builder(
                        Component.translatable("gui.allianceering.alliance_ring.akuma_alert"),
                        btn -> triggerAkumaAlert())
                .tooltip(Tooltip.create(Component.literal("Broadcast Akuma Warning with your live coordinates!")))
                .bounds(centerX - 48, this.topPos + 38, 96, 22)
                .build();

        this.sosHelpButton = Button.builder(
                        Component.translatable("gui.allianceering.alliance_ring.sos_help"),
                        btn -> triggerSosHelp())
                .tooltip(Tooltip.create(Component.literal("Broadcast Emergency SOS with your live coordinates!")))
                .bounds(centerX - 48, this.topPos + 74, 96, 22)
                .build();

        // Add all widgets
        this.addRenderableWidget(callingDockButton);
        this.addRenderableWidget(homeDockButton);
        this.addRenderableWidget(messagingDockButton);
        this.addRenderableWidget(customDockButton);
        this.addRenderableWidget(sosDockButton);

        this.addRenderableWidget(customSelectSkinBtn);
        this.addRenderableWidget(customCustomSkinBtn);
        this.addRenderableWidget(customShopBtn);
        this.addRenderableWidget(suitToggleButton);
        this.addRenderableWidget(shopBackButton);

        this.addRenderableWidget(applyCostumeButton);
        this.addRenderableWidget(skinBackButton);

        this.addRenderableWidget(skinInputBox);
        this.addRenderableWidget(applyCustomSkinButton);
        this.addRenderableWidget(customInputBackButton);

        this.addRenderableWidget(chatBackToContactsBtn);
        this.addRenderableWidget(chatInputBox);
        this.addRenderableWidget(chatSendButton);

        this.addRenderableWidget(callBackToContactsBtn);
        this.addRenderableWidget(endCallButton);
        this.addRenderableWidget(answerCallButton);
        this.addRenderableWidget(declineCallButton);

        this.addRenderableWidget(akumaAlertButton);
        this.addRenderableWidget(sosHelpButton);

        updateButtonVisibility();
    }

    private List<String> getOnlinePlayerNames() {
        List<String> list = new ArrayList<>();
        if (this.minecraft != null && this.minecraft.getConnection() != null) {
            Collection<PlayerInfo> players = this.minecraft.getConnection().getOnlinePlayers();
            String self = (this.minecraft.player != null) ? this.minecraft.player.getName().getString() : "";
            for (PlayerInfo p : players) {
                String name = p.getProfile().getName();
                if (!name.isEmpty() && !name.equalsIgnoreCase(self) && !list.contains(name)) {
                    list.add(name);
                }
            }
        }
        return list;
    }

    private void setViewMode(ViewMode mode) {
        this.currentMode = mode;
        if (mode == ViewMode.SELECT_SKIN) {
            this.targetScrollIndex = selectedCharacterIndex;
            this.currentScrollPos = selectedCharacterIndex;
        }

        if (skinInputBox != null) {
            boolean isInputMode = (mode == ViewMode.CUSTOM_SKIN_INPUT);
            skinInputBox.setFocused(isInputMode);
            if (isInputMode) this.setFocused(skinInputBox);
        }
        if (chatInputBox != null) {
            boolean isChatMode = (mode == ViewMode.MESSAGING_CHAT);
            chatInputBox.setFocused(isChatMode);
            if (isChatMode) this.setFocused(chatInputBox);
        }

        playClickSound();
        updateButtonVisibility();
    }

    private void confirmSelectedSkin() {
        int itemCount = CHARACTERS.size();
        selectedCharacterIndex = (Math.round(currentScrollPos) % itemCount + itemCount) % itemCount;
        playClickSound();
        setViewMode(ViewMode.MAIN);
    }

    public static void receiveIncomingMessage(String sender, String text, long timestamp) {
        List<ChatBubble> chat = CONVERSATIONS.computeIfAbsent(sender, k -> new ArrayList<>());
        chat.add(new ChatBubble(sender, text, false, timestamp));
    }

    private void sendMessage() {
        if (chatInputBox == null || this.minecraft == null || this.minecraft.player == null) return;
        String text = chatInputBox.getValue().trim();
        if (text.isEmpty() || activeChatContact == null || activeChatContact.isEmpty()) return;

        String selfName = this.minecraft.player.getName().getString();
        List<ChatBubble> chat = CONVERSATIONS.computeIfAbsent(activeChatContact, k -> new ArrayList<>());
        chat.add(new ChatBubble(selfName, text, true, System.currentTimeMillis()));

        // Dispatch over network to recipient & chat notification
        PacketDistributor.sendToServer(new AllianceSendMessagePayload(activeChatContact, text));

        chatInputBox.setValue("");
        this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.NOTE_BLOCK_PLING, 1.4f));
    }

    private void startCallWith(String contact) {
        if (contact == null || contact.isEmpty()) return;
        activeCallContact = contact;
        isCallConnected = false;
        callStartTime = System.currentTimeMillis();
        setViewMode(ViewMode.CALLING_ACTIVE);

        // Send call request packet to server
        PacketDistributor.sendToServer(new AllianceCallPayload(contact));

        if (this.minecraft != null && this.minecraft.player != null) {
            this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.NOTE_BLOCK_BELL.value(), 1.0f));
        }
    }

    private void endCurrentCall() {
        PacketDistributor.sendToServer(new AllianceEndCallPayload("hangup"));
        isCallConnected = false;
        activeCallContact = "";
        pendingIncomingCaller = "";

        if (this.minecraft != null && this.minecraft.player != null) {
            this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.NOTE_BLOCK_BASS, 0.8f));
        }
        setViewMode(ViewMode.CALLING_CONTACTS);
    }

    private void triggerAkumaAlert() {
        if (this.minecraft == null || this.minecraft.player == null) return;
        int x = (int) this.minecraft.player.getX();
        int y = (int) this.minecraft.player.getY();
        int z = (int) this.minecraft.player.getZ();

        String alertMsg = "[AKUMA ALERT] There is an Akuma at X: " + x + ", Y: " + y + ", Z: " + z + "!";
        this.minecraft.player.connection.sendChat(alertMsg);
        this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.RAID_HORN, 1.2f));
        playClickSound();
    }

    private void triggerSosHelp() {
        if (this.minecraft == null || this.minecraft.player == null) return;
        String playerName = this.minecraft.player.getName().getString();
        int x = (int) this.minecraft.player.getX();
        int y = (int) this.minecraft.player.getY();
        int z = (int) this.minecraft.player.getZ();

        String sosMsg = "[SOS] " + playerName + " needs help at X: " + x + ", Y: " + y + ", Z: " + z + "!";
        this.minecraft.player.connection.sendChat(sosMsg);
        this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BEACON_DEACTIVATE, 1.5f));
        playClickSound();
    }

    /**
     * Converts a raw 64x64 Minecraft skin into a flat 2D front avatar (160x320)
     * where Head is Y=0..79, Torso is Y=80..199, and Legs are Y=200..319.
     */
    private NativeImage generate2DFrontAvatar(NativeImage rawSkin, boolean slim) {
        int w = rawSkin.getWidth();
        int h = rawSkin.getHeight();

        if (w != 64 || (h != 64 && h != 32)) {
            return rawSkin;
        }

        NativeImage avatar = new NativeImage(160, 320, true);
        boolean hasLayer2 = (h >= 64);

        // Head (8x8 -> 80x80)
        copyRegion(rawSkin, 8, 8, 8, 8, avatar, 40, 0, 80, 80);
        if (hasLayer2) copyRegion(rawSkin, 40, 8, 8, 8, avatar, 40, 0, 80, 80);

        // Torso (8x12 -> 80x120)
        copyRegion(rawSkin, 20, 20, 8, 12, avatar, 40, 80, 80, 120);
        if (hasLayer2) copyRegion(rawSkin, 20, 36, 8, 12, avatar, 40, 80, 80, 120);

        // Arms (3x12 or 4x12 -> 30/40x120)
        int armSrcW = slim ? 3 : 4;
        int armDstW = slim ? 30 : 40;
        int rightArmX = slim ? 10 : 0;
        int leftArmX = 120;

        copyRegion(rawSkin, 44, 20, armSrcW, 12, avatar, rightArmX, 80, armDstW, 120);
        if (hasLayer2) copyRegion(rawSkin, 44, 36, armSrcW, 12, avatar, rightArmX, 80, armDstW, 120);

        if (hasLayer2) {
            copyRegion(rawSkin, 36, 52, armSrcW, 12, avatar, leftArmX, 80, armDstW, 120);
            copyRegion(rawSkin, 52, 52, armSrcW, 12, avatar, leftArmX, 80, armDstW, 120);
        } else {
            copyRegion(rawSkin, 44, 20, armSrcW, 12, avatar, leftArmX, 80, armDstW, 120);
        }

        // Legs (4x12 -> 40x120 each)
        copyRegion(rawSkin, 4, 20, 4, 12, avatar, 40, 200, 40, 120);
        if (hasLayer2) copyRegion(rawSkin, 4, 36, 4, 12, avatar, 40, 200, 40, 120);

        if (hasLayer2) {
            copyRegion(rawSkin, 20, 52, 4, 12, avatar, 80, 200, 40, 120);
            copyRegion(rawSkin, 4, 52, 4, 12, avatar, 80, 200, 40, 120);
        } else {
            copyRegion(rawSkin, 4, 20, 4, 12, avatar, 80, 200, 40, 120);
        }

        return avatar;
    }

    private void copyRegion(NativeImage src, int sx, int sy, int sw, int sh, NativeImage dst, int dx, int dy, int dw, int dh) {
        for (int y = 0; y < dh; y++) {
            for (int x = 0; x < dw; x++) {
                int srcX = sx + (x * sw) / dw;
                int srcY = sy + (y * sh) / dh;
                int color = src.getPixelRGBA(srcX, srcY);
                int alpha = (color >> 24) & 0xFF;
                if (alpha >= 250) {
                    dst.setPixelRGBA(dx + x, dy + y, color);
                } else if (alpha > 10) {
                    int oldColor = dst.getPixelRGBA(dx + x, dy + y);
                    int oldA = (oldColor >> 24) & 0xFF;
                    if (oldA == 0) {
                        dst.setPixelRGBA(dx + x, dy + y, color);
                    } else {
                        int r1 = color & 0xFF, g1 = (color >> 8) & 0xFF, b1 = (color >> 16) & 0xFF;
                        int r2 = oldColor & 0xFF, g2 = (oldColor >> 8) & 0xFF, b2 = (oldColor >> 16) & 0xFF;
                        int aOut = alpha + (oldA * (255 - alpha)) / 255;
                        int rOut = (r1 * alpha + r2 * oldA * (255 - alpha) / 255) / (aOut > 0 ? aOut : 1);
                        int gOut = (g1 * alpha + g2 * oldA * (255 - alpha) / 255) / (aOut > 0 ? aOut : 1);
                        int bOut = (b1 * alpha + b2 * oldA * (255 - alpha) / 255) / (aOut > 0 ? aOut : 1);
                        dst.setPixelRGBA(dx + x, dy + y, (aOut << 24) | (bOut << 16) | (gOut << 8) | rOut);
                    }
                }
            }
        }
    }

    /**
     * Detects slim (Alex) vs wide (Steve) model using Mojang's session server API.
     * Returns true for slim model, false for wide/classic model.
     */
    private boolean detectSlimFromMojangApi(String username) {
        try {
            // Step 1: Get UUID from username
            URL uuidUrl = new URI("https://api.mojang.com/users/profiles/minecraft/" + username).toURL();
            java.net.HttpURLConnection uuidConn = (java.net.HttpURLConnection) uuidUrl.openConnection();
            uuidConn.setRequestProperty("User-Agent", "AllianceRingMod/1.0");
            uuidConn.setConnectTimeout(5000);
            uuidConn.setReadTimeout(5000);

            if (uuidConn.getResponseCode() != 200) return false;
            String uuidJson;
            try (InputStream s = uuidConn.getInputStream()) {
                uuidJson = new String(s.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
            }
            com.google.gson.JsonObject uuidObj = com.google.gson.JsonParser.parseString(uuidJson).getAsJsonObject();
            if (!uuidObj.has("id")) return false;
            String uuid = uuidObj.get("id").getAsString();

            // Step 2: Get profile with textures property
            URL profileUrl = new URI("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid).toURL();
            java.net.HttpURLConnection profileConn = (java.net.HttpURLConnection) profileUrl.openConnection();
            profileConn.setRequestProperty("User-Agent", "AllianceRingMod/1.0");
            profileConn.setConnectTimeout(5000);
            profileConn.setReadTimeout(5000);

            if (profileConn.getResponseCode() != 200) return false;
            String profileJson;
            try (InputStream s = profileConn.getInputStream()) {
                profileJson = new String(s.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
            }

            // Step 3: Extract base64-encoded textures property and decode
            com.google.gson.JsonObject profileObj = com.google.gson.JsonParser.parseString(profileJson).getAsJsonObject();
            if (profileObj.has("properties")) {
                for (com.google.gson.JsonElement propElem : profileObj.getAsJsonArray("properties")) {
                    com.google.gson.JsonObject prop = propElem.getAsJsonObject();
                    if ("textures".equals(prop.get("name").getAsString())) {
                        String base64Value = prop.get("value").getAsString();
                        String decoded = new String(java.util.Base64.getDecoder().decode(base64Value), java.nio.charset.StandardCharsets.UTF_8);
                        return decoded.contains("\"slim\"");
                    }
                }
            }
            return false;

        } catch (Exception e) {
            AllianceRingMod.LOGGER.debug("Mojang slim detection failed for {}: {}", username, e.getMessage());
            return false;
        }
    }

    /**
     * Pixel-based slim detection fallback.
     * Checks the unused pixel area in Minecraft skin format that wide skins have but slim skins don't.
     */
    private boolean detectSlimFromPixels(NativeImage skin) {
        if (skin.getWidth() < 64 || skin.getHeight() < 64) return false;
        // In Alex (Slim 3px) skins:
        // Row 55 has empty pixels in the left arm area (cols 48..55 are alpha 0)
        // Row 39 has empty pixels in the right arm area (cols 44..52 are alpha 0)
        int a1 = (skin.getPixelRGBA(50, 55) >> 24) & 0xFF;
        int a2 = (skin.getPixelRGBA(52, 55) >> 24) & 0xFF;
        int a3 = (skin.getPixelRGBA(48, 39) >> 24) & 0xFF;
        if (a1 == 0 && a2 == 0 && a3 == 0) return true;

        // Also check layer 1 right arm unused column 54 & 55:
        int b1 = (skin.getPixelRGBA(54, 20) >> 24) & 0xFF;
        int b2 = (skin.getPixelRGBA(55, 20) >> 24) & 0xFF;
        return (b1 == 0 && b2 == 0);
    }

    private void fetchCustomSkin(String input) {
        if (input == null || input.trim().isEmpty()) return;
        String cleanInput = input.trim();
        playClickSound();

        CompletableFuture.runAsync(() -> {
            try {
                boolean isUrl = cleanInput.startsWith("http://") || cleanInput.startsWith("https://");
                String skinUrlStr = isUrl ? cleanInput : ("https://minotar.net/skin/" + cleanInput);

                // Detect slim model: use Mojang API for usernames, fallback to pixel check
                boolean slim = false;
                if (!isUrl) {
                    try {
                        slim = detectSlimFromMojangApi(cleanInput);
                    } catch (Exception ignored) {}
                }

                URL url = new URI(skinUrlStr).toURL();
                java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
                conn.setRequestProperty("User-Agent", "AllianceRingMod/1.0");
                conn.setConnectTimeout(8000);
                conn.setReadTimeout(8000);

                try (InputStream stream = conn.getInputStream()) {
                    NativeImage rawSkin = NativeImage.read(stream);
                    // If not confirmed slim from Mojang API, check pixel heuristics:
                    if (!slim) {
                        slim = detectSlimFromPixels(rawSkin);
                    }
                    CUSTOM_RAW_SKINS.put(cleanInput.toUpperCase(), rawSkin);
                    NativeImage frontAvatar = generate2DFrontAvatar(rawSkin, slim);
                    registerAndAddCharacter(cleanInput.toUpperCase(), frontAvatar, slim);
                }
            } catch (Exception e) {
                AllianceRingMod.LOGGER.error("Failed to fetch avatar for: " + cleanInput, e);
            }
        });
    }

    private void registerAndAddCharacter(String displayName, NativeImage image, boolean slim) {
        DynamicTexture dynamicTexture = new DynamicTexture(image);
        String safeName = displayName.replaceAll("[^a-zA-Z0-9_]", "_").toLowerCase();
        ResourceLocation loc = ResourceLocation.fromNamespaceAndPath(
                AllianceRingMod.MODID, "custom_avatar_" + safeName + "_" + System.currentTimeMillis()
        );
        Minecraft.getInstance().execute(() -> {
            Minecraft.getInstance().getTextureManager().register(loc, dynamicTexture);
            CHARACTERS.add(new CharacterEntry(displayName, loc, slim));
            selectedCharacterIndex = CHARACTERS.size() - 1;
            setViewMode(ViewMode.MAIN);
        });
    }

    private void updateButtonVisibility() {
        // Bottom dock is visible across ALL views
        if (homeDockButton != null) homeDockButton.visible = true;
        if (customDockButton != null) customDockButton.visible = true;
        if (messagingDockButton != null) messagingDockButton.visible = true;
        if (callingDockButton != null) callingDockButton.visible = true;
        if (sosDockButton != null) sosDockButton.visible = true;

        // Customization Hub
        if (customSelectSkinBtn != null) customSelectSkinBtn.visible = (currentMode == ViewMode.CUSTOMIZATION);
        if (customCustomSkinBtn != null) customCustomSkinBtn.visible = (currentMode == ViewMode.CUSTOMIZATION);
        if (customShopBtn != null) customShopBtn.visible = (currentMode == ViewMode.CUSTOMIZATION);
        if (suitToggleButton != null) {
            suitToggleButton.visible = (currentMode == ViewMode.CUSTOMIZATION);
            if (suitToggleButton.visible && !CHARACTERS.isEmpty()) {
                boolean activeIsSlim = CHARACTERS.get(selectedCharacterIndex).isSlim();
                suitToggleButton.setTooltip(Tooltip.create(Component.literal(activeIsSlim ? "Model: Slim" : "Model: Wide")));
            }
        }

        // Shop View
        if (shopBackButton != null) shopBackButton.visible = (currentMode == ViewMode.SHOP);

        // Select Skin
        if (applyCostumeButton != null) applyCostumeButton.visible = (currentMode == ViewMode.SELECT_SKIN);
        if (skinBackButton != null) skinBackButton.visible = (currentMode == ViewMode.SELECT_SKIN);

        // Custom Skin Input
        if (skinInputBox != null) skinInputBox.visible = (currentMode == ViewMode.CUSTOM_SKIN_INPUT);
        if (applyCustomSkinButton != null) applyCustomSkinButton.visible = (currentMode == ViewMode.CUSTOM_SKIN_INPUT);
        if (customInputBackButton != null) customInputBackButton.visible = (currentMode == ViewMode.CUSTOM_SKIN_INPUT);

        // Messages Chat View
        if (chatBackToContactsBtn != null) chatBackToContactsBtn.visible = (currentMode == ViewMode.MESSAGING_CHAT);
        if (chatInputBox != null) chatInputBox.visible = (currentMode == ViewMode.MESSAGING_CHAT);
        if (chatSendButton != null) chatSendButton.visible = (currentMode == ViewMode.MESSAGING_CHAT);

        // Calling View
        if (callBackToContactsBtn != null) callBackToContactsBtn.visible = (currentMode == ViewMode.CALLING_ACTIVE);
        if (endCallButton != null) endCallButton.visible = (currentMode == ViewMode.CALLING_ACTIVE);
        if (answerCallButton != null) answerCallButton.visible = (currentMode == ViewMode.CALLING_INCOMING);
        if (declineCallButton != null) declineCallButton.visible = (currentMode == ViewMode.CALLING_INCOMING);

        // SOS
        if (akumaAlertButton != null) akumaAlertButton.visible = (currentMode == ViewMode.SOS);
        if (sosHelpButton != null) sosHelpButton.visible = (currentMode == ViewMode.SOS);
    }

    private void toggleSelectedCharacterModel() {
        if (CHARACTERS.isEmpty()) return;
        CharacterEntry active = CHARACTERS.get(selectedCharacterIndex);
        boolean newSlim = !active.isSlim();

        ResourceLocation newTexture = active.texture();
        NativeImage raw = CUSTOM_RAW_SKINS.get(active.name());
        if (raw != null) {
            NativeImage newAvatar = generate2DFrontAvatar(raw, newSlim);
            DynamicTexture dyn = new DynamicTexture(newAvatar);
            newTexture = ResourceLocation.fromNamespaceAndPath(
                    AllianceRingMod.MODID, "custom_avatar_" + active.name().replaceAll("[^a-zA-Z0-9_]", "_").toLowerCase() + "_" + System.currentTimeMillis()
            );
            Minecraft.getInstance().getTextureManager().register(newTexture, dyn);
        }

        CHARACTERS.set(selectedCharacterIndex, new CharacterEntry(active.name(), newTexture, newSlim));
        if (this.suitToggleButton != null) {
            this.suitToggleButton.setTooltip(Tooltip.create(Component.literal(newSlim ? "Model: Slim" : "Model: Wide")));
        }
        playClickSound();
    }

    private void playClickSound() {
        if (this.minecraft != null) {
            this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0f));
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        float guiScale = calculateGuiScale();
        double scaledMouseX = mouseX;
        double scaledMouseY = mouseY;
        if (guiScale < 1.0f) {
            float cX = this.width / 2.0f;
            float cY = this.height / 2.0f;
            scaledMouseX = cX + (mouseX - cX) / guiScale;
            scaledMouseY = cY + (mouseY - cY) / guiScale;
        }

        if (currentMode == ViewMode.SELECT_SKIN && scrollY != 0) {
            int itemCount = CHARACTERS.size();
            if (scrollY < 0) {
                targetScrollIndex = (targetScrollIndex + 1) % itemCount;
            } else {
                targetScrollIndex = (targetScrollIndex - 1 + itemCount) % itemCount;
            }
            playClickSound();
            return true;
        }
        return super.mouseScrolled(scaledMouseX, scaledMouseY, scrollX, scrollY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        float guiScale = calculateGuiScale();
        double scaledMouseX = mouseX;
        double scaledMouseY = mouseY;
        if (guiScale < 1.0f) {
            float cX = this.width / 2.0f;
            float cY = this.height / 2.0f;
            scaledMouseX = cX + (mouseX - cX) / guiScale;
            scaledMouseY = cY + (mouseY - cY) / guiScale;
        }

        if (button == 0) {
            int centerX = this.leftPos + PANEL_WIDTH / 2;
            int centerY = this.topPos + CAROUSEL_CENTER_Y_OFFSET;

            int centerCardX = centerX - CENTER_WIDTH / 2;
            int centerCardY = centerY - CENTER_HEIGHT / 2;

            int leftCardX = centerX - 30;
            int rightCardX = centerX + 30;
            int sideCardY = centerY - 20;

            if (currentMode == ViewMode.SELECT_SKIN) {
                int itemCount = CHARACTERS.size();
                if (scaledMouseX >= centerCardX && scaledMouseX <= centerCardX + CENTER_WIDTH &&
                    scaledMouseY >= centerCardY && scaledMouseY <= centerCardY + CENTER_HEIGHT) {
                    confirmSelectedSkin();
                    return true;
                } else if (scaledMouseX >= leftCardX - 10 && scaledMouseX <= leftCardX + 10 && scaledMouseY >= sideCardY && scaledMouseY <= sideCardY + 40) {
                    targetScrollIndex = (targetScrollIndex - 1 + itemCount) % itemCount;
                    playClickSound();
                    return true;
                } else if (scaledMouseX >= rightCardX - 10 && scaledMouseX <= rightCardX + 10 && scaledMouseY >= sideCardY && scaledMouseY <= sideCardY + 40) {
                    targetScrollIndex = (targetScrollIndex + 1) % itemCount;
                    playClickSound();
                    return true;
                }
            } else if (currentMode == ViewMode.MESSAGING_CONTACTS) {
                List<String> players = getOnlinePlayerNames();
                int startY = this.topPos + 24;
                int itemH = 16;
                int boxW = 92;
                int boxX = centerX - boxW / 2;

                for (int i = 0; i < players.size(); i++) {
                    int itemY = startY + i * (itemH + 4);
                    if (scaledMouseX >= boxX && scaledMouseX <= boxX + boxW &&
                        scaledMouseY >= itemY && scaledMouseY <= itemY + itemH) {
                        activeChatContact = players.get(i);
                        setViewMode(ViewMode.MESSAGING_CHAT);
                        return true;
                    }
                }
            } else if (currentMode == ViewMode.CALLING_CONTACTS) {
                List<String> players = getOnlinePlayerNames();
                int startY = this.topPos + 24;
                int itemH = 16;
                int boxW = 92;
                int boxX = centerX - boxW / 2;

                for (int i = 0; i < players.size(); i++) {
                    int itemY = startY + i * (itemH + 4);
                    if (scaledMouseX >= boxX && scaledMouseX <= boxX + boxW &&
                        scaledMouseY >= itemY && scaledMouseY <= itemY + itemH) {
                        startCallWith(players.get(i));
                        return true;
                    }
                }
            }
        }
        return super.mouseClicked(scaledMouseX, scaledMouseY, button);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (currentMode == ViewMode.CUSTOM_SKIN_INPUT && skinInputBox != null && skinInputBox.isFocused()) {
            return skinInputBox.charTyped(codePoint, modifiers);
        }
        if (currentMode == ViewMode.MESSAGING_CHAT && chatInputBox != null) {
            return chatInputBox.charTyped(codePoint, modifiers);
        }
        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (currentMode == ViewMode.CUSTOM_SKIN_INPUT && skinInputBox != null && skinInputBox.isFocused()) {
            if (keyCode == 257) { // Enter key
                fetchCustomSkin(skinInputBox.getValue());
                return true;
            }
            if (skinInputBox.keyPressed(keyCode, scanCode, modifiers)) {
                return true;
            }
            return true;
        }
        if (currentMode == ViewMode.MESSAGING_CHAT) {
            if (keyCode == 257 || keyCode == 335) { // Enter or Keypad Enter
                sendMessage();
                return true;
            }
            if (chatInputBox != null && chatInputBox.keyPressed(keyCode, scanCode, modifiers)) {
                return true;
            }
            return true;
        }

        if (super.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        if (this.minecraft != null && this.minecraft.options.keyInventory.matches(keyCode, scanCode)) {
            this.onClose();
            return true;
        }
        return false;
    }

    @Override
    protected void renderBlurredBackground(float partialTick) {
    }

    private void drawCleanString(GuiGraphics graphics, Component text, int centerX, int y) {
        String str = text.getString();
        int width = this.font.width(str);
        int drawX = centerX - width / 2;
        graphics.drawString(this.font, text, drawX, y, 0xFFFFFFFF, true);
    }

    /**
     * Renders the avatar with its 1:1 tailored white suit overlay.
     * The white suit starts precisely at the shoulders/torso (Y >= 80) with an open V-neck collar,
     * leaving the head, face, eyes, and chin 100% uncovered and visible!
     */
    private void renderCharacterAvatar(GuiGraphics graphics, CharacterEntry character, int x, int y, int w, int h) {
        // 1. Base Character Skin
        graphics.blit(
                character.texture(),
                x, y,
                0.0f, 0.0f,
                w, h,
                w, h
        );

        // 2. White Suit Overlay (Matches character model: Wide for classic, Slim for Alex)
        // LILA_SKIN already has her long hair falling gracefully over the white suit
        if (!character.texture().equals(LILA_SKIN)) {
            ResourceLocation suitTex = character.isSlim() ? WHITE_SUIT_SLIM : WHITE_SUIT_WIDE;
            graphics.blit(
                    suitTex,
                    x, y,
                    0.0f, 0.0f,
                    w, h,
                    w, h
            );
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        float guiScale = calculateGuiScale();

        graphics.pose().pushPose();
        if (guiScale < 1.0f) {
            float cX = this.width / 2.0f;
            float cY = this.height / 2.0f;
            graphics.pose().translate(cX, cY, 0);
            graphics.pose().scale(guiScale, guiScale, 1.0f);
            graphics.pose().translate(-cX, -cY, 0);
        }

        double scaledMouseX = mouseX;
        double scaledMouseY = mouseY;
        if (guiScale < 1.0f) {
            float cX = this.width / 2.0f;
            float cY = this.height / 2.0f;
            scaledMouseX = cX + (mouseX - cX) / guiScale;
            scaledMouseY = cY + (mouseY - cY) / guiScale;
        }

        if (currentMode == ViewMode.SELECT_SKIN) {
            int itemCount = CHARACTERS.size();
            float diff = targetScrollIndex - currentScrollPos;
            if (diff > itemCount / 2.0f) diff -= itemCount;
            if (diff < -itemCount / 2.0f) diff += itemCount;

            currentScrollPos += diff * 0.22f;
            if (Math.abs(diff) < 0.005f) {
                currentScrollPos = targetScrollIndex;
            }
        }

        // 1. Render Clean Top Card Background
        graphics.blit(
                BACKGROUND_TEXTURE,
                this.leftPos, this.topPos,
                0.0f, 0.0f,
                PANEL_WIDTH, PANEL_HEIGHT,
                PANEL_WIDTH, PANEL_HEIGHT
        );

        int centerX = this.leftPos + PANEL_WIDTH / 2;

        // Sub-screen Dark Mask
        if (currentMode != ViewMode.MAIN && currentMode != ViewMode.CUSTOMIZATION && currentMode != ViewMode.SELECT_SKIN) {
            graphics.fill(this.leftPos + 2, this.topPos + 2, this.leftPos + PANEL_WIDTH - 2, this.topPos + PANEL_HEIGHT - 2, 0xDD111111);
        }

        // 2. Render Screen Title
        if (currentMode != ViewMode.MESSAGING_CHAT && currentMode != ViewMode.CALLING_ACTIVE && currentMode != ViewMode.CALLING_INCOMING) {
            Component titleComponent = switch (currentMode) {
                case SELECT_SKIN -> Component.translatable("gui.allianceering.alliance_ring.costume_title");
                case CUSTOMIZATION -> Component.translatable("gui.allianceering.alliance_ring.custom_title");
                case CUSTOM_SKIN_INPUT -> Component.translatable("gui.allianceering.alliance_ring.custom_skin_title");
                case SHOP -> Component.literal("Alliance Shop");
                case MESSAGING_CONTACTS -> Component.literal("Messages Contacts");
                case CALLING_CONTACTS -> Component.literal("Phone Contacts");
                case SOS -> Component.translatable("gui.allianceering.alliance_ring.sos_title");
                default -> this.title;
            };
            drawCleanString(graphics, titleComponent, centerX, this.topPos + 6);
        }

        int carouselCenterY = this.topPos + CAROUSEL_CENTER_Y_OFFSET;

        if (currentMode == ViewMode.MAIN || currentMode == ViewMode.CUSTOMIZATION) {
            CharacterEntry activeChar = CHARACTERS.get(selectedCharacterIndex);
            int cardX = centerX - CENTER_WIDTH / 2;
            int cardY = carouselCenterY - CENTER_HEIGHT / 2;

            boolean isHovering = scaledMouseX >= cardX && scaledMouseX < cardX + CENTER_WIDTH && scaledMouseY >= cardY && scaledMouseY < cardY + CENTER_HEIGHT;
            if (isHovering) {
                graphics.fill(cardX - 1, cardY - 1, cardX + CENTER_WIDTH + 1, cardY + CENTER_HEIGHT + 1, 0x33FFFFFF);
                graphics.renderOutline(cardX - 2, cardY - 2, CENTER_WIDTH + 4, CENTER_HEIGHT + 4, 0x88FFFFFF);
            }

            renderCharacterAvatar(graphics, activeChar, cardX, cardY, CENTER_WIDTH, CENTER_HEIGHT);

        } else if (currentMode == ViewMode.CUSTOM_SKIN_INPUT) {
            CharacterEntry activeChar = CHARACTERS.get(selectedCharacterIndex);
            int cardX = centerX - CENTER_WIDTH / 2;
            int cardY = this.topPos + 20;

            renderCharacterAvatar(graphics, activeChar, cardX, cardY, CENTER_WIDTH, CENTER_HEIGHT);
            drawCleanString(graphics, Component.translatable("gui.allianceering.alliance_ring.enter_name_or_url"), centerX, this.topPos + 72);

        } else if (currentMode == ViewMode.SELECT_SKIN) {
            int itemCount = CHARACTERS.size();
            for (int order = 0; order < 2; order++) {
                for (int relOffset = -1; relOffset <= 1; relOffset++) {
                    boolean isCenter = (relOffset == 0);

                    if ((order == 0 && isCenter) || (order == 1 && !isCenter)) {
                        continue;
                    }

                    int baseIndex = Math.round(currentScrollPos);
                    int i = ((baseIndex + relOffset) % itemCount + itemCount) % itemCount;

                    float offset = relOffset - (currentScrollPos - baseIndex);
                    float scale = 1.0f - 0.375f * Math.min(1.0f, Math.abs(offset));
                    int cardW = Math.round(CENTER_WIDTH * scale);
                    int cardH = Math.round(CENTER_HEIGHT * scale);

                    int cardX = centerX + (int) (offset * 30.0f) - cardW / 2;
                    int cardY = carouselCenterY - cardH / 2;

                    CharacterEntry charEntry = CHARACTERS.get(i);

                    boolean isHovering = scaledMouseX >= cardX && scaledMouseX < cardX + cardW && scaledMouseY >= cardY && scaledMouseY < cardY + cardH;
                    if (isHovering) {
                        graphics.fill(cardX - 1, cardY - 1, cardX + cardW + 1, cardY + cardH + 1, 0x33FFFFFF);
                        graphics.renderOutline(cardX - 2, cardY - 2, cardW + 4, cardH + 4, 0x88FFFFFF);
                    }

                    renderCharacterAvatar(graphics, charEntry, cardX, cardY, cardW, cardH);
                }
            }

            int activeIndex = (Math.round(currentScrollPos) % itemCount + itemCount) % itemCount;
            String previewName = CHARACTERS.get(activeIndex).name();
            int badgeY = carouselCenterY + CENTER_HEIGHT / 2 + 2;

            graphics.fill(centerX - 30, badgeY - 1, centerX + 30, badgeY + 10, 0x55000000);
            drawCleanString(graphics, Component.literal(previewName), centerX, badgeY);
            drawCleanString(graphics, Component.translatable("gui.allianceering.alliance_ring.scroll_hint"), centerX, badgeY + 12);

        } else if (currentMode == ViewMode.MESSAGING_CONTACTS || currentMode == ViewMode.CALLING_CONTACTS) {
            List<String> players = getOnlinePlayerNames();
            int startY = this.topPos + 24;
            int itemH = 16;
            int boxW = 92;
            int boxX = centerX - boxW / 2;

            if (players.isEmpty()) {
                graphics.fill(boxX, startY + 18, boxX + boxW, startY + 48, 0xAA222222);
                graphics.renderOutline(boxX, startY + 18, boxW, 30, 0x66FFFFFF);
                drawCleanString(graphics, Component.literal("No players online"), centerX, startY + 28);

            } else {
                for (int i = 0; i < players.size(); i++) {
                    String name = players.get(i);
                    int itemY = startY + i * (itemH + 4);

                    boolean isHovering = scaledMouseX >= boxX && scaledMouseX <= boxX + boxW && scaledMouseY >= itemY && scaledMouseY <= itemY + itemH;
                    int bg = isHovering ? 0xAA444444 : 0x88222222;
                    graphics.fill(boxX, itemY, boxX + boxW, itemY + itemH, bg);
                    graphics.renderOutline(boxX, itemY, boxW, itemH, isHovering ? 0xFF5B9E2B : 0x66FFFFFF);

                    // Online indicator
                    graphics.fill(boxX + 5, itemY + 5, boxX + 11, itemY + 11, 0xFF4CAF50);
                    graphics.drawString(this.font, name, boxX + 16, itemY + 4, 0xFFFFFFFF, true);

                    String actionLabel = (currentMode == ViewMode.MESSAGING_CONTACTS) ? "💬" : "📞";
                    graphics.drawString(this.font, actionLabel, boxX + boxW - 14, itemY + 4, 0xFFFFFFFF, true);
                }
            }

        } else if (currentMode == ViewMode.MESSAGING_CHAT) {
            int headerX = this.leftPos + 20;
            int headerY = this.topPos + 6;
            int headerW = PANEL_WIDTH - 24;
            int headerH = 12;

            graphics.fill(headerX, headerY, headerX + headerW, headerY + headerH, 0xFF5B9E2B);
            graphics.renderOutline(headerX, headerY, headerW, headerH, 0xFF000000);
            drawCleanString(graphics, Component.literal(activeChatContact.isEmpty() ? "Messages" : activeChatContact), headerX + headerW / 2, headerY + 2);

            int chatAreaX = this.leftPos + 4;
            int chatAreaY = this.topPos + 22;
            int chatAreaW = PANEL_WIDTH - 8;
            int chatAreaH = 102;

            graphics.fill(chatAreaX, chatAreaY, chatAreaX + chatAreaW, chatAreaY + chatAreaH, 0xAA222222);
            graphics.renderOutline(chatAreaX, chatAreaY, chatAreaW, chatAreaH, 0xFF444444);

            List<ChatBubble> chat = CONVERSATIONS.getOrDefault(activeChatContact, Collections.emptyList());
            int maxVisible = 5;
            int startIndex = Math.max(0, chat.size() - maxVisible);

            int bubbleY = chatAreaY + 4;
            for (int i = startIndex; i < chat.size(); i++) {
                ChatBubble b = chat.get(i);
                int textW = this.font.width(b.text());
                int bWidth = Math.min(chatAreaW - 10, textW + 10);
                int bHeight = 15;

                int bX = b.isSelf() ? (chatAreaX + chatAreaW - bWidth - 3) : (chatAreaX + 3);
                int bColor = b.isSelf() ? 0xFF5B9E2B : 0xFF383838;

                graphics.fill(bX, bubbleY, bX + bWidth, bubbleY + bHeight, bColor);
                graphics.renderOutline(bX, bubbleY, bWidth, bHeight, 0xFF000000);

                graphics.drawString(this.font, b.text(), bX + 4, bubbleY + 4, 0xFFFFFFFF, true);
                bubbleY += bHeight + 4;
            }

        } else if (currentMode == ViewMode.CALLING_ACTIVE) {
            int headerX = this.leftPos + 20;
            int headerY = this.topPos + 6;
            int headerW = PANEL_WIDTH - 24;
            int headerH = 12;

            graphics.fill(headerX, headerY, headerX + headerW, headerY + headerH, 0xFF2196F3);
            graphics.renderOutline(headerX, headerY, headerW, headerH, 0xFF000000);
            drawCleanString(graphics, Component.literal("Call: " + activeCallContact), headerX + headerW / 2, headerY + 2);

            int callAreaX = this.leftPos + 4;
            int callAreaY = this.topPos + 22;
            int callAreaW = PANEL_WIDTH - 8;
            int callAreaH = 92;

            graphics.fill(callAreaX, callAreaY, callAreaX + callAreaW, callAreaY + callAreaH, 0xAA1E293B);
            graphics.renderOutline(callAreaX, callAreaY, callAreaW, callAreaH, 0xFF38BDF8);

            graphics.blit(ICON_CALLING, centerX - 14, callAreaY + 12, 0.0f, 0.0f, 28, 28, 28, 28);

            if (isCallConnected) {
                drawCleanString(graphics, Component.literal("§a● Voice Chat Connected"), centerX, callAreaY + 46);
                long elapsedSec = (System.currentTimeMillis() - callStartTime) / 1000;
                String timeStr = String.format("%02d:%02d", elapsedSec / 60, elapsedSec % 60);
                drawCleanString(graphics, Component.literal("Duration: " + timeStr), centerX, callAreaY + 62);
            } else {
                drawCleanString(graphics, Component.literal("§e● Ringing..."), centerX, callAreaY + 46);
                drawCleanString(graphics, Component.literal("§7Waiting for answer..."), centerX, callAreaY + 62);
            }

        } else if (currentMode == ViewMode.CALLING_INCOMING) {
            int callAreaX = this.leftPos + 4;
            int callAreaY = this.topPos + 18;
            int callAreaW = PANEL_WIDTH - 8;
            int callAreaH = 96;

            graphics.fill(callAreaX, callAreaY, callAreaX + callAreaW, callAreaY + callAreaH, 0xAA1E293B);
            graphics.renderOutline(callAreaX, callAreaY, callAreaW, callAreaH, 0xFF38BDF8);

            graphics.blit(ICON_CALLING, centerX - 14, callAreaY + 10, 0.0f, 0.0f, 28, 28, 28, 28);
            drawCleanString(graphics, Component.literal("§e📞 Incoming Call:"), centerX, callAreaY + 42);
            drawCleanString(graphics, Component.literal("§f" + pendingIncomingCaller), centerX, callAreaY + 54);

        } else if (currentMode == ViewMode.SHOP) {
            int shopAreaX = this.leftPos + 4;
            int shopAreaY = this.topPos + 20;
            int shopAreaW = PANEL_WIDTH - 8;
            int shopAreaH = 100;

            graphics.fill(shopAreaX, shopAreaY, shopAreaX + shopAreaW, shopAreaY + shopAreaH, 0xAA1E293B);
            graphics.renderOutline(shopAreaX, shopAreaY, shopAreaW, shopAreaH, 0xFF38BDF8);

            // Clean centered 2x2 grid of placeholder shop items
            int gridSlotW = 40;
            int gridSlotH = 38;
            int startX = centerX - gridSlotW - 2;
            int startY = shopAreaY + 10;

            for (int r = 0; r < 2; r++) {
                for (int c = 0; c < 2; c++) {
                    int sx = startX + c * (gridSlotW + 4);
                    int sy = startY + r * (gridSlotH + 6);
                    boolean isHover = scaledMouseX >= sx && scaledMouseX <= sx + gridSlotW && scaledMouseY >= sy && scaledMouseY <= sy + gridSlotH;
                    graphics.fill(sx, sy, sx + gridSlotW, sy + gridSlotH, isHover ? 0xAA2A3A4D : 0x881E293B);
                    graphics.renderOutline(sx, sy, gridSlotW, gridSlotH, isHover ? 0xFF38BDF8 : 0x6638BDF8);
                    drawCleanString(graphics, Component.literal("§b📦"), sx + gridSlotW / 2, sy + 6);
                    drawCleanString(graphics, Component.literal("§fSoon"), sx + gridSlotW / 2, sy + 18);
                    drawCleanString(graphics, Component.literal("§8Slot " + (r * 2 + c + 1)), sx + gridSlotW / 2, sy + 28);
                }
            }

        } else if (currentMode == ViewMode.SOS) {
            int x = (this.minecraft != null && this.minecraft.player != null) ? (int) this.minecraft.player.getX() : 0;
            int y = (this.minecraft != null && this.minecraft.player != null) ? (int) this.minecraft.player.getY() : 0;
            int z = (this.minecraft != null && this.minecraft.player != null) ? (int) this.minecraft.player.getZ() : 0;

            int badgeW = 92;
            graphics.fill(centerX - badgeW / 2, this.topPos + 18, centerX + badgeW / 2, this.topPos + 32, 0xAA222222);
            graphics.renderOutline(centerX - badgeW / 2, this.topPos + 18, badgeW, 14, 0x66FFFFFF);
            drawCleanString(graphics, Component.literal("§eCoords: §fX:" + x + " Y:" + y + " Z:" + z), centerX, this.topPos + 21);

            drawCleanString(graphics, Component.literal("§7Broadcasts location to chat"), centerX, this.topPos + 64);
            drawCleanString(graphics, Component.literal("§7Alerts nearby heroes for help"), centerX, this.topPos + 102);
        }

        // Render widgets & tooltips
        super.render(graphics, (int) scaledMouseX, (int) scaledMouseY, partialTick);

        graphics.pose().popPose();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
