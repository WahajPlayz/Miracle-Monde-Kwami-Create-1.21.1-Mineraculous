package com.allianceering.network;

import com.allianceering.AllianceRingMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.Holder;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record AlliancePowerPayload(String powerId) implements CustomPacketPayload {
    public static final Type<AlliancePowerPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(AllianceRingMod.MODID, "alliance_power"));

    public static final StreamCodec<ByteBuf, AlliancePowerPayload> STREAM_CODEC = ByteBufCodecs.STRING_UTF8.map(AlliancePowerPayload::new, AlliancePowerPayload::powerId);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(AlliancePowerPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            if (player == null) return;

            switch (payload.powerId()) {
                case "ox" -> {
                    applyEffect(player, MobEffects.DAMAGE_RESISTANCE, 600, 1);
                    applyEffect(player, MobEffects.FIRE_RESISTANCE, 600, 0);
                }
                case "tiger" -> {
                    applyEffect(player, MobEffects.DAMAGE_BOOST, 600, 1);
                    applyEffect(player, MobEffects.DIG_SPEED, 600, 1);
                }
                case "horse" -> {
                    applyEffect(player, MobEffects.MOVEMENT_SPEED, 600, 2);
                    applyEffect(player, MobEffects.JUMP, 600, 1);
                }
                case "fox" -> {
                    applyEffect(player, MobEffects.INVISIBILITY, 600, 0);
                    applyEffect(player, MobEffects.NIGHT_VISION, 600, 0);
                }
                case "rooster" -> {
                    applyEffect(player, MobEffects.REGENERATION, 600, 1);
                    applyEffect(player, MobEffects.SATURATION, 100, 0);
                }
            }
        });
    }

    private static void applyEffect(Player player, Holder<MobEffect> effect, int duration, int amplifier) {
        player.addEffect(new MobEffectInstance(effect, duration, amplifier, false, true, true));
    }
}
