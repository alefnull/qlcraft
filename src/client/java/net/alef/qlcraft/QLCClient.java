package net.alef.qlcraft;

import net.alef.qlcraft.items.RailgunItem;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.event.client.player.ClientPreAttackCallback;

public class QLCClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
        ClientPreAttackCallback.EVENT.register(((client, player, clickCount) -> {
            if (player.getMainHandStack().getItem() instanceof RailgunItem && clickCount == 1) {
                if (player.getItemCooldownManager().isCoolingDown(player.getMainHandStack())) {
                    return false; // prevent multiple firings at the same time
                }
                player.getItemCooldownManager().set(player.getMainHandStack(), 20); // 1 second cooldown
                FireRailgunPayload payload = new FireRailgunPayload();
                ClientPlayNetworking.send(payload);
                return true;
            }
            return false;
        }));
    }
}