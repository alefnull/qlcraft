package net.alef.qlcraft;

import net.alef.qlcraft.items.RailgunItem;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.event.client.player.ClientPreAttackCallback;

public class QLCClient implements ClientModInitializer {

    @Override
	public void onInitializeClient() {
        //* handle railgun primary fire on left-click
        ClientPreAttackCallback.EVENT.register(((client, player, clickCount) -> {
            if (player.getMainHandStack().getItem() instanceof RailgunItem && clickCount == 1) {
                if (player.getItemCooldownManager().isCoolingDown(player.getMainHandStack())) {
                    return false;
                }
                player.getItemCooldownManager().set(player.getMainHandStack(), 30);
                QLCraft.NETWORK_CHANNEL.clientHandle().send(new QLCraft.FireRailgunPacket(QLCraft.CONFIG.instagibRailgun()));
                return true;
            }
            return false;
        }));
    }
}