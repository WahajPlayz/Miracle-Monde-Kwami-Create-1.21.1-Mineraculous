package com.example.exampleaddon.data.advancements;

import com.example.exampleaddon.data.advancements.packs.ExampleAdvancements;
import com.google.common.collect.ImmutableSet;
import dev.thomasglasser.tommylib.api.data.advancements.ExtendedAdvancementProvider;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

/// Generates custom advancements for this addon.
public class ExampleAdvancementProvider extends ExtendedAdvancementProvider {
    public ExampleAdvancementProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries, BiConsumer<String, String> lang, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, registries, existingFileHelper, ImmutableSet.of(
                new ExampleAdvancements(lang)));
    }
}
