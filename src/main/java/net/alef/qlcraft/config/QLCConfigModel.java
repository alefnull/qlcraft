package net.alef.qlcraft.config;

import io.wispforest.owo.config.Option;
import io.wispforest.owo.config.annotation.Config;
import io.wispforest.owo.config.annotation.Modmenu;
import io.wispforest.owo.config.annotation.Sync;

//* configuration model for QLCraft mod
@Modmenu(modId = "qlcraft")
@Config(name = "qlcraft", wrapperName = "QLCConfig")
public class QLCConfigModel {
    //* instagibRailgun: if true, railgun will instantly kill any entity it hits
    @Sync(Option.SyncMode.OVERRIDE_CLIENT)
    public boolean instagibRailgun = false;
}