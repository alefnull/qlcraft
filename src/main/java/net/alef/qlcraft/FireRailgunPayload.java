package net.alef.qlcraft;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record FireRailgunPayload() implements CustomPayload {
    public static final Identifier FIRE_RAILGUN_PACKET_ID = Identifier.of(QLCraft.MOD_ID, "fire_railgun");
    public static final CustomPayload.Id<FireRailgunPayload> ID = new CustomPayload.Id<>(FIRE_RAILGUN_PACKET_ID);

    public static final PacketCodec<PacketByteBuf, FireRailgunPayload> CODEC = PacketCodec.of(FireRailgunPayload::write, FireRailgunPayload::read);

    public static void write(FireRailgunPayload payload, PacketByteBuf buf) {
        // intentionally empty, no data to write, the payload is the trigger
    }

    public static FireRailgunPayload read(PacketByteBuf buf) {
        return new FireRailgunPayload();
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
