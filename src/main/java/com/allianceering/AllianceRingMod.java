package com.allianceering;

import com.allianceering.command.AllianceCommands;
import com.allianceering.item.AllianceRingItem;
import com.allianceering.network.AllianceAnswerCallPayload;
import com.allianceering.network.AllianceCallConnectedPayload;
import com.allianceering.network.AllianceCallPayload;
import com.allianceering.network.AllianceEndCallPayload;
import com.allianceering.network.AllianceIncomingCallPayload;
import com.allianceering.network.AllianceReceiveMessagePayload;
import com.allianceering.network.AllianceSendMessagePayload;
import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;

@Mod(AllianceRingMod.MODID)
public class AllianceRingMod {
    public static final String MODID = "allianceering";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public static final DeferredItem<Item> ALLIANCE_RING = ITEMS.register("alliance_ring",
            () -> new AllianceRingItem(new Item.Properties().stacksTo(1)));

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> ALLIANCE_RING_TAB = CREATIVE_MODE_TABS.register("alliance_ring_tab",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.allianceering"))
                    .withTabsBefore(CreativeModeTabs.COMBAT)
                    .icon(() -> ALLIANCE_RING.get().getDefaultInstance())
                    .displayItems((parameters, output) -> output.accept(ALLIANCE_RING.get()))
                    .build());

    public AllianceRingMod(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::registerPayloads);

        ITEMS.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);

        modEventBus.addListener(this::addCreative);

        // Register gameplay commands on the NeoForge event bus
        NeoForge.EVENT_BUS.addListener(this::registerCommands);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        LOGGER.info("Alliance Ring mod loaded");
    }

    private void registerCommands(RegisterCommandsEvent event) {
        AllianceCommands.register(event.getDispatcher());
    }

    private void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1.0.0");

        registrar.playToServer(
                AllianceSendMessagePayload.TYPE,
                AllianceSendMessagePayload.STREAM_CODEC,
                AllianceSendMessagePayload::handle);
        registrar.playToClient(
                AllianceReceiveMessagePayload.TYPE,
                AllianceReceiveMessagePayload.STREAM_CODEC,
                AllianceReceiveMessagePayload::handle);
        registrar.playToServer(
                AllianceCallPayload.TYPE,
                AllianceCallPayload.STREAM_CODEC,
                AllianceCallPayload::handle);
        registrar.playToClient(
                AllianceIncomingCallPayload.TYPE,
                AllianceIncomingCallPayload.STREAM_CODEC,
                AllianceIncomingCallPayload::handle);
        registrar.playToServer(
                AllianceAnswerCallPayload.TYPE,
                AllianceAnswerCallPayload.STREAM_CODEC,
                AllianceAnswerCallPayload::handle);
        registrar.playToClient(
                AllianceCallConnectedPayload.TYPE,
                AllianceCallConnectedPayload.STREAM_CODEC,
                AllianceCallConnectedPayload::handle);
        registrar.playToServer(
                AllianceEndCallPayload.TYPE,
                AllianceEndCallPayload.STREAM_CODEC,
                AllianceEndCallPayload::handle);
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.COMBAT) {
            event.accept(ALLIANCE_RING);
        }
    }
}
