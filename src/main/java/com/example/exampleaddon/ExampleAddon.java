package com.example.exampleaddon;

import com.example.exampleaddon.data.ExampleDataGenerators;
import com.example.exampleaddon.world.item.ExampleCreativeModeTabs;
import com.example.exampleaddon.world.item.ExampleItems;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The main entry point for your Mineraculous addon.
 *
 * <p>The value provided to the {@link Mod} annotation must match the {@code modId} specified
 * in your {@code resources/META-INF/neoforge.mods.toml} file.
 */
@Mod(ExampleAddon.MOD_ID)
public class ExampleAddon {
    public static final String MOD_ID = "exampleaddon";
    public static final String MOD_NAME = "Example Addon";
    private static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

    /**
     * Mod constructor. NeoForge dependency injection passes the mod event bus and mod container.
     *
     * @param modEventBus  The event bus for mod lifecycle events (registration, setup, data gen).
     * @param modContainer Metadata container for this mod.
     */
    public ExampleAddon(IEventBus modEventBus, ModContainer modContainer) {
        LOGGER.info(
                "Initializing {} for {} in a {} environment...",
                MOD_NAME,
                TommyLibServices.PLATFORM.getPlatformName(),
                TommyLibServices.PLATFORM.getEnvironmentName());

        // Initialize registries by referencing their classes or calling an init() method.
        // Doing this early ensures DeferredRegisters are loaded and ready before registration events fire.
        ExampleItems.init();
        ExampleCreativeModeTabs.init();

        // Listen for mod setup lifecycle events on the modEventBus
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(ExampleDataGenerators::onGatherData);
        modEventBus.addListener(this::onBuildCreativeModeTabContents);

        // Listen for in-game runtime events on NeoForge's global EVENT_BUS
        NeoForge.EVENT_BUS.addListener(this::onServerStarting);
    }

    /**
     * Common setup runs after items, blocks, and basic registries have been registered.
     * Use this for networking, setting up integrations, or cross-mod interactions.
     */
    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("{} common setup completed successfully.", MOD_NAME);
    }

    /**
     * Event listener triggered when creative mode tab contents are built.
     * Use this to inject your addon's items into vanilla tabs or other mods' tabs.
     */
    private void onBuildCreativeModeTabContents(final BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(ExampleItems.EXAMPLE_JEWEL.toStack());
        }
    }

    /// Example runtime event listener triggered when a Minecraft server begins starting.
    private void onServerStarting(final ServerStartingEvent event) {
        LOGGER.info("{} says: Server is starting! Ready to transform!", MOD_NAME);
    }

    /**
     * Helper method to create a {@link ResourceLocation} under this addon's namespace.
     *
     * @param path The resource path (e.g., "example_item").
     * @return A ResourceLocation formatted as "exampleaddon:path".
     */
    public static ResourceLocation modLoc(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
