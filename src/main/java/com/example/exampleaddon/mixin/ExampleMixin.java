package com.example.exampleaddon.mixin;

import com.example.exampleaddon.ExampleAddon;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Example Mixin demonstrating how to modify Minecraft or library bytecode at runtime.
 *
 * <p>Mixins are registered in your {@code resources/<mod_id>.mixins.json} file.
 * The {@link Mixin} annotation specifies the target class whose bytecode you want to modify.
 */
@Mixin(MinecraftServer.class)
public class ExampleMixin {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExampleAddon.MOD_NAME + " Mixin");

    /**
     * Injects custom code at the HEAD (start) of {@link MinecraftServer#loadLevel()}.
     *
     * @param info Callback metadata provided by the Mixin framework.
     */
    @Inject(at = @At("HEAD"), method = "loadLevel")
    private void exampleaddon$onLoadLevel(CallbackInfo info) {
        // Notice: Prefixing your mixin method name with your modid (e.g., exampleaddon$...)
        // prevents method signature collisions with other mods injecting into the same class!
        LOGGER.info("ExampleMixin fired: MinecraftServer is beginning level load!");
    }
}
