package net.alef.qlcraft;

import net.alef.qlcraft.items.ModItems;
import net.alef.qlcraft.items.RailgunItem;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.stat.Stats;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QLCraft implements ModInitializer {
	public static final String MOD_ID = "qlcraft";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
	public void onInitialize() {
		LOGGER.info("Initializing QLCraft...");
        ModItems.registerModItems();
        Registry.register(Registries.SOUND_EVENT, Identifier.of(MOD_ID, "rail_fire"),
                SoundEvent.of(Identifier.of(MOD_ID, "rail_fire")));

        PayloadTypeRegistry.playC2S().register(FireRailgunPayload.ID, FireRailgunPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(FireRailgunPayload.ID, (payload, context) -> {
            if (context.player().getMainHandStack().getItem() instanceof RailgunItem railgun) {
                if (context.player().getItemCooldownManager().isCoolingDown(context.player().getMainHandStack())) {
                    return;
                }
                railgun.firePrimary(context.player());
                context.player().incrementStat(Stats.USED.getOrCreateStat(railgun));
            }
        });
	}
}