package net.alef.qlcraft;

import net.alef.qlcraft.items.RailgunItem;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.event.client.player.ClientPreAttackCallback;

import java.awt.*;

public class QLCClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
        ClientPreAttackCallback.EVENT.register(((client, player, clickCount) -> {
            if (player.getMainHandStack().getItem() instanceof RailgunItem && clickCount == 1) {
                if (player.getItemCooldownManager().isCoolingDown(player.getMainHandStack())) {
                    return false;
                }
                player.getItemCooldownManager().set(player.getMainHandStack(), 30); // 1 second cooldown at 20 ticks per second
                FireRailgunPayload payload = new FireRailgunPayload();
                ClientPlayNetworking.send(payload);
                return true;
            }
            return false;
        }));
    }
}