package com.example.exampleaddon.world.kamikotization;

import com.example.exampleaddon.ExampleAddon;
import com.example.exampleaddon.world.item.ExampleItems;
import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Either;
import dev.thomasglasser.mineraculous.api.core.customization.setting.CustomizationSettings;
import dev.thomasglasser.mineraculous.api.core.registries.MineraculousRegistries;
import dev.thomasglasser.mineraculous.api.world.kamikotization.Kamikotization;
import dev.thomasglasser.mineraculous.api.world.kamikotization.condition.ItemCondition;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;

/**
 * Demonstrates how to define and register a custom {@link Kamikotization} via data generation.
 *
 * <p>In Mineraculous, Kamikotizations (when a kamiko powers up an object into a superpowered form)
 * are data-driven. We define the conditions required for a Kamikotization to trigger and what item/ability it produces.
 */
public class ExampleKamikotizations {
    /// ResourceKey representing our custom Example Kamikotization.
    public static final ResourceKey<Kamikotization> EXAMPLE = create("example");

    private static ResourceKey<Kamikotization> create(String name) {
        return ResourceKey.create(MineraculousRegistries.KAMIKOTIZATION, ExampleAddon.modLoc(name));
    }

    /**
     * Bootstraps our custom Kamikotization definitions for the datapack registry generator.
     *
     * @param context The bootstrap context provided by data generation.
     */
    public static void bootstrap(BootstrapContext<Kamikotization> context) {
        context.register(
                EXAMPLE,
                new Kamikotization(
                        // 1. Conditions list: Conditions checked when a Kamikotization attempts to trigger
                        // Here, we require the target entity to be holding exactly 1 of our Example Jewel.
                        ImmutableList.of(
                                new ItemCondition(
                                        ExampleItems.EXAMPLE_JEWEL.get(),
                                        predicate -> predicate.withCount(MinMaxBounds.Ints.exactly(1)))),
                        // 2. Customization settings (empty by default unless adding specific animation/sound tweaks)
                        CustomizationSettings.EMPTY,
                        // 3. Power source: The ItemStack tool or Ability that provides the main power
                        Either.left(ExampleItems.EXAMPLE_JEWEL.toStack()),
                        // 4. Passive abilities: A list of passive Abilities granted to the Kamikotized entity
                        ImmutableList.of()));
    }
}
