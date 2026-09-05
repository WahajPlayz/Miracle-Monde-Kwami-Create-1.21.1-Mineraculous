package com.allianceering.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;

/**
 * The Alliance Ring item. Currently uses a plain stick model/texture as a placeholder;
 * right-clicking it opens the Alliance / Ring GUI on the client.
 */
public class AllianceRingItem extends Item {
    public AllianceRingItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResultHolder.pass(stack);
        }

        if (level.isClientSide()) {
            player.playSound(net.minecraft.sounds.SoundEvents.AMETHYST_BLOCK_CHIME, 1.0F, 1.2F);
            if (FMLEnvironment.dist == Dist.CLIENT) {
                openScreen();
            }
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    // Kept in a separate method so client-only classes (Minecraft, Screen) are never
    // loaded/referenced on the dedicated server.
    private void openScreen() {
        com.allianceering.client.AllianceRingClientHandler.openAllianceRingScreen();
    }
}
