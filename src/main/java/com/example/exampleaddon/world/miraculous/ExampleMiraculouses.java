package com.example.exampleaddon.world.miraculous;

import com.example.exampleaddon.ExampleAddon;
import com.example.exampleaddon.world.item.ExampleItems;
import com.google.common.collect.ImmutableList;
import dev.thomasglasser.mineraculous.api.core.customization.setting.CustomizationSettingKeys;
import dev.thomasglasser.mineraculous.api.core.customization.setting.CustomizationSettings;
import dev.thomasglasser.mineraculous.api.core.registries.MineraculousRegistries;
import dev.thomasglasser.mineraculous.api.sounds.MineraculousSoundEvents;
import dev.thomasglasser.mineraculous.api.world.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.impl.data.curios.MineraculousCuriosProvider;
import dev.thomasglasser.mineraculous.impl.world.ability.BuiltInAbilities;
import java.util.Optional;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceKey;

/**
 * Demonstrates how to define and register a custom {@link Miraculous} via data generation.
 *
 * <p>In Mineraculous, Miraculouses are data-driven registry objects defined in datapacks.
 * We define their {@link ResourceKey}s here and generate the corresponding JSON resources
 * during data generation (see {@code ExampleDataGenerators}).
 */
public class ExampleMiraculouses {
    /// ResourceKey representing our custom Example Miraculous.
    public static final ResourceKey<Miraculous> EXAMPLE = create("example");

    private static ResourceKey<Miraculous> create(String name) {
        return ResourceKey.create(MineraculousRegistries.MIRACULOUS, ExampleAddon.modLoc(name));
    }

    /**
     * Bootstraps our custom Miraculous definitions for the datapack registry generator.
     *
     * @param context The bootstrap context provided by data generation.
     */
    public static void bootstrap(BootstrapContext<Miraculous> context) {
        context.register(
                EXAMPLE,
                new Miraculous(
                        // 1. TextColor: Theme color used for chat formatting, UI, and visual effects
                        TextColor.fromRgb(0x00ffcc),
                        // 2. Curios slot: Which Curio accessory slot this Miraculous occupies (e.g., brooch, ring, necklace)
                        MineraculousCuriosProvider.SLOT_BROOCH,
                        // 3. Tool/Weapon ItemStack: The item granted to the player when transformed
                        ExampleItems.EXAMPLE_JEWEL.toStack(),
                        // 4. Optional secondary slot (e.g., belt holster for weapons when not held)
                        Optional.empty(),
                        // 5. CustomizationSettings: Configures transformation animation duration and sound events
                        CustomizationSettings.builder()
                                .add(CustomizationSettingKeys.TRANSFORMATION_FRAMES, 7)
                                .add(CustomizationSettingKeys.TRANSFORM_SOUND, MineraculousSoundEvents.BUTTERFLY_TRANSFORM)
                                .build(),
                        // 6. Active Superpower Ability: The main ability triggered by the hero (e.g., Kamikotization, Cataclysm)
                        BuiltInAbilities.KAMIKOTIZATION,
                        // 7. Passive Abilities: Constant buffs or powers active while transformed
                        ImmutableList.of(
                                BuiltInAbilities.KAMIKO_CONTROL,
                                BuiltInAbilities.KAMIKOTIZED_COMMUNICATION)));
    }
}
