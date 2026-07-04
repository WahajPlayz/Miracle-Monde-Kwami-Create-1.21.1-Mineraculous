package com.example.exampleaddon.world.item;

import com.example.exampleaddon.ExampleAddon;
import dev.thomasglasser.tommylib.api.registration.DeferredItem;
import dev.thomasglasser.tommylib.api.registration.DeferredRegister;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

/**
 * Holds all registered {@link Item}s for this addon.
 *
 * <p>We use TommyLib's {@link DeferredRegister} wrapper to automatically handle item registration across loaders.
 */
public class ExampleItems {
    /**
     * The item register for our mod namespace.
     * When initialized, it automatically registers with the platform's underlying registration system.
     */
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ExampleAddon.MOD_ID);

    /**
     * An example item (e.g., a custom Miraculous jewel or tool).
     *
     * <p>{@link DeferredItem} is a type-safe supplier that provides the registered item instance
     * after Minecraft's registration phase has completed.
     */
    public static final DeferredItem<Item> EXAMPLE_JEWEL = ITEMS.register(
            "example_jewel",
            () -> new Item(new Item.Properties()
                    .stacksTo(1)
                    .rarity(Rarity.RARE)));

    /**
     * Called from {@link ExampleAddon}'s constructor to force class initialization and ensure
     * our register is created early.
     */
    public static void init() {}
}
