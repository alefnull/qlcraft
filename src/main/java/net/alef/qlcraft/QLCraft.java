package net.alef.qlcraft;

import com.mojang.brigadier.arguments.BoolArgumentType;
import io.wispforest.owo.itemgroup.Icon;
import io.wispforest.owo.itemgroup.OwoItemGroup;
import io.wispforest.owo.network.OwoNetChannel;
import net.alef.qlcraft.config.QLCConfig;
import net.alef.qlcraft.items.ModItems;
import net.alef.qlcraft.items.RailgunItem;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.command.CommandManager;
import net.minecraft.sound.SoundEvent;
import net.minecraft.stat.Stats;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QLCraft implements ModInitializer {
    public static final String MOD_ID = "qlcraft";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final QLCConfig CONFIG = QLCConfig.createAndLoad();
    public static final OwoNetChannel NETWORK_CHANNEL = OwoNetChannel.create(Identifier.of(MOD_ID, "network_channel"));
    public record FireRailgunPacket(boolean instagib) {}

    public static final Icon GROUP_ICON = Icon.of(id("textures/group_icon.png"), 16, 16, false);

    //* item group for QLCraft items
    //* assign to group by calling ".group(QLCraft.QLCRAFT_GROUP)" on item settings
    //* when registering blocks/items in ModItems/ModBlocks class (if i add blocks)
    public static final OwoItemGroup QLCRAFT_GROUP = OwoItemGroup.builder(
            id("item_group"),
            () -> GROUP_ICON).disableDynamicTitle().build();

    //* main mod initialization method
    @Override
    public void onInitialize() {
        LOGGER.info("Initializing QLCraft...");

        //* initialize item group and items
        QLCRAFT_GROUP.initialize();
        ModItems.init();

        //* register server-bound packet for firing railgun
        //* sent from client when player left-clicks with railgun in hand
        NETWORK_CHANNEL.registerServerbound(FireRailgunPacket.class, ((message, access) -> {
            if (access.player().getMainHandStack().getItem() instanceof RailgunItem railgun) {
                if (access.player().getItemCooldownManager().isCoolingDown(access.player().getMainHandStack())) {
                    return;
                }
                QLCraft.CONFIG.instagibRailgun(message.instagib);
                railgun.firePrimary(access.player());
                access.player().incrementStat(Stats.USED.getOrCreateStat(railgun));
            }
        }));

        //* register /qlcraft command and any sub-commands
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
            dispatcher.register(
                CommandManager.literal("qlcraft")
                    //* instagib sub-command to toggle instagib railgun mode
                    .then(CommandManager.argument("instagib", BoolArgumentType.bool()))
                        .executes(commandContext -> {
                            boolean enabled = BoolArgumentType.getBool(commandContext, "instagib");
                            QLCraft.CONFIG.instagibRailgun(enabled);
                            return 1;
                        })
            ));

        //* register railgun firing sound event
        Registry.register(Registries.SOUND_EVENT, Identifier.of(MOD_ID, "rail_fire"), SoundEvent.of(Identifier.of(MOD_ID, "rail_fire")));
    }

    //* helper method to create identifiers with mod id prefix
    public static Identifier id(String... path) {
        return Identifier.of(MOD_ID, String.join("/", path));
    }
}