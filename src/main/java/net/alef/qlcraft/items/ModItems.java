package net.alef.qlcraft.items;

import net.alef.qlcraft.QLCraft;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.function.Function;

public class ModItems {    public static final Item RAILGUN = register("railgun",
        RailgunItem::new,
        new Item.Settings().maxCount(1));

    public static final RegistryKey<ItemGroup> QLCRAFT_GROUP_KEY = RegistryKey.of(Registries.ITEM_GROUP.getKey(), Identifier.of(QLCraft.MOD_ID, "item_group"));
    public static final ItemGroup QLCRAFT_GROUP = FabricItemGroup.builder()
            .icon(() -> new ItemStack(RAILGUN))
            .displayName(Text.translatable("itemGroup.qlcraft"))
            .build();

    public static Item register(String name, Function<Item.Settings, Item> itemFactory, Item.Settings settings) {
        RegistryKey<Item> itemKey = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(QLCraft.MOD_ID, name));
        Item item = itemFactory.apply(settings.registryKey(itemKey));
        Registry.register(Registries.ITEM, itemKey, item);
        return item;
    }
    public static void registerModItems() {
        QLCraft.LOGGER.info("Registering QLCraft Items...");

        Registry.register(Registries.ITEM_GROUP, QLCRAFT_GROUP_KEY, QLCRAFT_GROUP);

        ItemGroupEvents.modifyEntriesEvent(QLCRAFT_GROUP_KEY).register(entries -> entries.add(RAILGUN));
    }
}
