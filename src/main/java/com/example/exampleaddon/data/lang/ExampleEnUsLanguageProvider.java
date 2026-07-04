package com.example.exampleaddon.data.lang;

import com.example.exampleaddon.ExampleAddon;
import com.example.exampleaddon.world.item.ExampleCreativeModeTabs;
import com.example.exampleaddon.world.item.ExampleItems;
import com.example.exampleaddon.world.kamikotization.ExampleKamikotizations;
import com.example.exampleaddon.world.miraculous.ExampleMiraculouses;
import dev.thomasglasser.mineraculous.api.world.kamikotization.condition.ItemCondition;
import dev.thomasglasser.mineraculous.impl.data.lang.MineraculousEnUsLanguageProvider;
import net.minecraft.data.PackOutput;

/**
 * Generates English (en_us) language translations for items, tabs, and datapack objects.
 *
 * <p>Extending {@link MineraculousEnUsLanguageProvider} provides convenient helper methods
 * for adding translations for Miraculouses, Kamikotizations, items, and creative tabs.
 */
public class ExampleEnUsLanguageProvider extends MineraculousEnUsLanguageProvider {
    public ExampleEnUsLanguageProvider(PackOutput output) {
        super(output, ExampleAddon.MOD_ID);
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
