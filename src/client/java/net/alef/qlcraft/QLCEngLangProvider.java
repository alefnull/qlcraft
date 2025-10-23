package net.alef.qlcraft;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.minecraft.registry.RegistryWrapper;

import java.util.concurrent.CompletableFuture;

public class QLCEngLangProvider extends FabricLanguageProvider {
    protected QLCEngLangProvider(FabricDataOutput dataOutput, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup) {
        super(dataOutput, registryLookup);
    }

    @Override
    public void generateTranslations(RegistryWrapper.WrapperLookup wrapperLookup, TranslationBuilder translationBuilder) {
        translationBuilder.add("item.qlcraft.railgun", "Railgun");
        translationBuilder.add("itemGroup.qlcraft.item_group", "QLCraft");
        translationBuilder.add("sound.qlcraft.rail_fire", "Railgun beam fire");
        translationBuilder.add("text.config.qlcraft.title", "QLCraft Options");
        translationBuilder.add("text.config.qlcraft.option.instagibRailgun", "Instagib Railgun");
    }
}
