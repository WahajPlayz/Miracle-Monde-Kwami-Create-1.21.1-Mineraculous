package com.example.exampleaddon.data.advancements.packs;

import com.example.exampleaddon.ExampleAddon;
import com.example.exampleaddon.world.item.ExampleItems;
import dev.thomasglasser.tommylib.api.data.advancements.ExtendedAdvancementGenerator;
import java.util.function.BiConsumer;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;

/// Defines the actual advancement tree and triggers for this addon.
public class ExampleAdvancements extends ExtendedAdvancementGenerator {
    public ExampleAdvancements(BiConsumer<String, String> lang) {
        super(ExampleAddon.MOD_ID, "main", lang);
    }

    @Override
    public void generate(HolderLookup.Provider provider) {
        AdvancementHolder root = builder("root", ExampleItems.EXAMPLE_JEWEL.toStack(), "Example Addon", "The beginning of your miraculous adventure")
                .background(ResourceLocation.withDefaultNamespace("textures/gui/advancements/backgrounds/stone.png"))
                .toast(false)
                .announce(false)
                .trigger("has_example_jewel", InventoryChangeTrigger.TriggerInstance.hasItems(ExampleItems.EXAMPLE_JEWEL.get()))
                .build();
    }
}
