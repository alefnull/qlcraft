package net.alef.qlcraft;

import net.alef.qlcraft.items.RailgunItem;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.fabric.api.event.client.player.ClientPreAttackCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.GlyphProvider;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.util.Identifier;

import java.awt.*;

public class QLCClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
        HudElementRegistry.attachElementBefore(VanillaHudElements.CHAT, Identifier.of(QLCraft.MOD_ID, "before_chat"), QLCClient::render);
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

    private static void render(DrawContext context, RenderTickCounter tickCounter) {
        // just a way to easily disply current version on screen for testing
        String version = "QLCraft v" + QLCraft.MOD_VERSION;
        int x = 2;
        int y = 2;
        int color = Color.WHITE.getRGB();
        context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, version, x, y, color);
    }
}