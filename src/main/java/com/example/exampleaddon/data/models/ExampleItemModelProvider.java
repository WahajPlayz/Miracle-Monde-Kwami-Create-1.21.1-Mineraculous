package com.example.exampleaddon.data.models;

import com.example.exampleaddon.ExampleAddon;
import com.example.exampleaddon.world.item.ExampleItems;
import dev.thomasglasser.tommylib.api.data.models.ExtendedItemModelProvider;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

/**
 * Generates item model JSON files (in {@code src/generated/resources/models/item/}) for this addon.
 *
 * <p>Extending TommyLib's {@link ExtendedItemModelProvider} gives us convenient helper methods
 * like {@link #basicItem} and advanced perspective model builders.
 */
public class ExampleItemModelProvider extends ExtendedItemModelProvider {
    public ExampleItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, ExampleAddon.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        // Generates a standard generated item model pointing to "item/example_jewel" texture
        basicItem(ExampleItems.EXAMPLE_JEWEL.get());
    }
}
