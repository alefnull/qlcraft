package net.alef.qlcraft.items;

import io.wispforest.owo.itemgroup.OwoItemSettingsExtension;
import io.wispforest.owo.registration.reflect.AutoRegistryContainer;
import net.alef.qlcraft.QLCraft;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

import java.util.function.Function;

public class ModItems implements AutoRegistryContainer<Item> {
    public static final Item RAILGUN = register("railgun", RailgunItem::new, new Item.Settings().maxCount(1).group(QLCraft.QLCRAFT_GROUP));

    //* registration helper methods
    public static <T extends Item> T register(String path, Function<Item.Settings, T> factory) {
        return register(path, factory, new Item.Settings());
    }
    public static Item register(String path, Item.Settings settings) {
        return register(Identifier.of(QLCraft.MOD_ID, path), Item::new, settings);
    }
    public static <T extends Item> T register(String path, Function<Item.Settings, T> factory, Item.Settings settings) {
        return register(Identifier.of(QLCraft.MOD_ID, path), factory, settings);
    }
    public static Item register(Identifier identifier, Item.Settings settings) {
        return register(identifier, Item::new, settings);
    }
    public static <T extends Item> T register(Identifier identifier, Function<Item.Settings, T> factory, Item.Settings settings) {
        var registryKey = RegistryKey.of(RegistryKeys.ITEM, identifier);

        settings.registryKey(registryKey);

        T t = factory.apply(settings);

        return Registry.register(Registries.ITEM, registryKey, t);
    }

    //* initialization method to be called from main mod class onInitialize method
    public static void init() {}

    //* AutoRegistryContainer implementation
    @Override
    public Registry<Item> getRegistry() {
        return Registries.ITEM;
    }

    @Override
    public Class<Item> getTargetFieldType() {
        return Item.class;
    }
}
