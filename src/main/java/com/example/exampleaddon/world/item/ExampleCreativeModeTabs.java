package com.example.exampleaddon.world.item;

import com.example.exampleaddon.ExampleAddon;
import dev.thomasglasser.mineraculous.impl.world.item.MineraculousCreativeModeTabs;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import dev.thomasglasser.tommylib.api.registration.DeferredHolder;
import dev.thomasglasser.tommylib.api.registration.DeferredRegister;
import java.util.Set;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackLinkedSet;

/// Holds all registered {@link CreativeModeTab}s for this addon.
public class ExampleCreativeModeTabs {
    /// DeferredRegister for creative mode tabs under our mod namespace.
    private static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ExampleAddon.MOD_ID);

    /**
     * Our addon's custom creative mode tab.
     *
     * <p>We use {@link TommyLibServices#CLIENT}'s tabBuilder() to construct a cross-loader tab.
     * Notice how we set type SEARCH and automatically collect all items from our addon namespace,
     * and position our tab right after Mineraculous's main tab using {@code withTabsBefore}.
     */
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> EXAMPLE_TAB = TABS.register(
            ExampleAddon.MOD_ID,
            () -> TommyLibServices.CLIENT
                    .tabBuilder()
                    .title(Component.translatable(
                            ExampleAddon.modLoc(ExampleAddon.MOD_ID).toLanguageKey("item_group")))
                    .icon(ExampleItems.EXAMPLE_JEWEL::toStack)
                    .type(CreativeModeTab.Type.SEARCH)
                    .displayItems((parameters, output) -> {
                        Set<ItemStack> set = ItemStackLinkedSet.createTypeAndComponentsSet();

                        parameters.holders().lookupOrThrow(Registries.CREATIVE_MODE_TAB).listElements().map(Holder::value).forEach(tab -> {
                            if (tab.getType() != CreativeModeTab.Type.SEARCH) {
                                for (ItemStack stack : tab.getSearchTabDisplayItems()) {
                                    if (BuiltInRegistries.ITEM.getKey(stack.getItem()).getNamespace().equals(ExampleAddon.MOD_ID)) {
                                        set.add(stack);
                                    }
                                }
                            }
                        });

                        output.acceptAll(set);
                    })
                    .withTabsBefore(MineraculousCreativeModeTabs.MINERACULOUS.getKey())
                    .build());

    /// Called from {@link ExampleAddon}'s constructor to force class initialization.
    public static void init() {}
}
