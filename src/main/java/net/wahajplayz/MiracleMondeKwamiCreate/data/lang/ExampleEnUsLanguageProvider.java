package net.wahajplayz.MiracleMondeKwamiCreate.data.lang;

import dev.thomasglasser.mineraculous.api.world.kamikotization.condition.ItemCondition;
import dev.thomasglasser.mineraculous.impl.data.lang.MineraculousEnUsLanguageProvider;
import net.minecraft.data.PackOutput;
import net.wahajplayz.MiracleMondeKwamiCreate.MiracleMondeKwamiCreate;
import net.wahajplayz.MiracleMondeKwamiCreate.world.item.ExampleCreativeModeTabs;
import net.wahajplayz.MiracleMondeKwamiCreate.world.item.ExampleItems;
import net.wahajplayz.MiracleMondeKwamiCreate.world.kamikotization.ExampleKamikotizations;
import net.wahajplayz.MiracleMondeKwamiCreate.world.miraculous.ExampleMiraculouses;

/**
 * Generates English (en_us) language translations for items, tabs, and datapack objects.
 *
 * <p>Extending {@link MineraculousEnUsLanguageProvider} provides convenient helper methods
 * for adding translations for Miraculouses, Kamikotizations, items, and creative tabs.
 */
public class ExampleEnUsLanguageProvider extends MineraculousEnUsLanguageProvider {
    public ExampleEnUsLanguageProvider(PackOutput output) {
        super(output, MiracleMondeKwamiCreate.MOD_ID);
    }

    @Override
    protected void addTranslations() {
        addItems();
        addTabs();
        addMiraculouses();
        addKamikotizations();
    }

    protected void addItems() {
        add(ExampleItems.EXAMPLE_JEWEL.get(), "Example Jewel");
    }

    protected void addTabs() {
        add(ExampleCreativeModeTabs.EXAMPLE_TAB.get(), "Example Addon");
    }

    protected void addMiraculouses() {
        add(ExampleMiraculouses.EXAMPLE, "Example");
    }

    protected void addKamikotizations() {
        add(ExampleKamikotizations.EXAMPLE, "Example");
        add(ItemCondition.requiresMessage(ExampleItems.EXAMPLE_JEWEL.get()), "Requires an Example Jewel");
    }
}
