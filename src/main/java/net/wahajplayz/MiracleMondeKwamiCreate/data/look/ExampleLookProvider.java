package NET.wahajplayz.MiracleMondeKwamiCreate.data.look;

import NET.wahajplayz.MiracleMondeKwamiCreate.MiracleMondeKwamiCreate;
import NET.wahajplayz.MiracleMondeKwamiCreate.world.item.ExampleItems;
import NET.wahajplayz.MiracleMondeKwamiCreate.world.kamikotization.ExampleKamikotizations;
import NET.wahajplayz.MiracleMondeKwamiCreate.world.miraculous.ExampleMiraculouses;
import dev.thomasglasser.mineraculous.api.core.registries.MineraculousRegistries;
import dev.thomasglasser.mineraculous.impl.data.looks.MineraculousLookProvider;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;

/**
 * Data generation provider for Miraculous and Kamikotization visual looks.
 *
 * <p>Looks define the visual appearance, models, animations, shaders, and textures
 * associated with Miraculouses and Kamikotized entities.
 */
public class ExampleLookProvider extends MineraculousLookProvider {
    public ExampleLookProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, MiracleMondeKwamiCreate.MOD_ID, lookupProvider);
    }

    @Override
    protected void registerLooks(HolderLookup.Provider provider) {
        // Register visual look for the Miraculous (automatically maps suit textures, 3D armor models,
        // hidden/powered item states, tool animations, and the Kwami entity's model/textures/animations).
        miraculousNoAnims(provider.lookupOrThrow(MineraculousRegistries.MIRACULOUS).getOrThrow(ExampleMiraculouses.EXAMPLE), ExampleItems.EXAMPLE_JEWEL.getKey());

        // Register visual look for the Kamikotization (maps superpowered armor models and suit textures).
        kamikotizationLookNoAnims(ExampleKamikotizations.EXAMPLE);
    }
}
