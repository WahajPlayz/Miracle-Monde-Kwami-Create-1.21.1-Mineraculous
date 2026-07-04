package NET.wahajplayz.MiracleMondeKwamiCreate.data;

import NET.wahajplayz.MiracleMondeKwamiCreate.MiracleMondeKwamiCreate;
import NET.wahajplayz.MiracleMondeKwamiCreate.data.advancements.ExampleAdvancementProvider;
import NET.wahajplayz.MiracleMondeKwamiCreate.data.lang.ExampleEnUsLanguageProvider;
import NET.wahajplayz.MiracleMondeKwamiCreate.data.look.ExampleLookProvider;
import NET.wahajplayz.MiracleMondeKwamiCreate.data.models.ExampleItemModelProvider;
import NET.wahajplayz.MiracleMondeKwamiCreate.world.kamikotization.ExampleKamikotizations;
import NET.wahajplayz.MiracleMondeKwamiCreate.world.miraculous.ExampleMiraculouses;
import dev.thomasglasser.mineraculous.api.core.registries.MineraculousRegistries;
import dev.thomasglasser.tommylib.api.data.DataGenerationUtils;
import net.minecraft.core.RegistrySetBuilder;
import net.neoforged.neoforge.data.event.GatherDataEvent;

/**
 * Handles data generation for this addon when running the 'Data Generation' run config.
 *
 * <p>Data generation automatically creates item models, language files, tags, advancements, looks, and datapack
 * registries (like Miraculouses and Kamikotizations) into {@code src/generated/resources/}.
 */
public class ExampleDataGenerators {
    /**
     * The RegistrySetBuilder bundles our data-driven bootstrap methods so NeoForge can generate
     * datapack JSON files for them.
     */
    private static final RegistrySetBuilder BUILDER = new RegistrySetBuilder()
            .add(MineraculousRegistries.MIRACULOUS, ExampleMiraculouses::bootstrap)
            .add(MineraculousRegistries.KAMIKOTIZATION, ExampleKamikotizations::bootstrap);

    /**
     * Called when the {@link GatherDataEvent} fires on the mod event bus.
     *
     * @param event The data generation event provided by NeoForge.
     */
    public static void onGatherData(GatherDataEvent event) {
        // 1. Generate datapack registry JSON files (for Miraculouses and Kamikotizations)
        event.createDatapackRegistryObjects(BUILDER);

        // 2. Generate a helpful registry dump report for debugging
        DataGenerationUtils.createRegistryDumpReport(event, MiracleMondeKwamiCreate.MOD_ID);

        // 3. Register common providers (language and dependent providers like advancements)
        DataGenerationUtils.createLangDependent(event, ExampleEnUsLanguageProvider::new, ExampleAdvancementProvider::new);

        // 4. Register client-side asset providers (e.g., item models and looks)
        DataGenerationUtils.createProvider(event, ExampleItemModelProvider::new);
        event.createProvider(ExampleLookProvider::new);
    }
}
