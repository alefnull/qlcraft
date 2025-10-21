// src/main/java/net/alef/qlcraft/items/BaseWeaponItem.java
package net.alef.qlcraft.items;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

import java.util.List;

public abstract class BaseWeaponItem extends Item {
    private static final double PARTICLE_STEP = 0.5;
    private static final double ENTITY_RAYCAST_STEP = 0.25;
    public boolean INSTAGIB = false; // set to true to enable instagib mode

    public BaseWeaponItem(Settings settings) {
        super(settings);
    }

    // called for left-click (primary fire)
    // must be called manually from packet handler on server side
    public void firePrimary(PlayerEntity player) {
        if (!player.getEntityWorld().isClient()) {
            if (!player.getItemCooldownManager().isCoolingDown(player.getMainHandStack())) {
                onPrimaryFire(player, INSTAGIB);
            }
        }
    }

    // called for right-click (secondary fire)
    @Override
    public ActionResult use(World world, PlayerEntity player, Hand hand) {
        if (!world.isClient()) {
            if (!player.getItemCooldownManager().isCoolingDown(player.getMainHandStack())) {
                onSecondaryFire(world, player, hand, INSTAGIB);
                return ActionResult.SUCCESS;
            }
        }
        return ActionResult.PASS;
    }

    // abstract methods for subclasses to implement
    protected abstract void onPrimaryFire(PlayerEntity player, boolean instagib);
    protected abstract void onSecondaryFire(World world, PlayerEntity player, Hand hand, boolean instagib);

    // raycast for blocks
    public static BlockHitResult raycastBlock(Entity camera, double range, float tickDelta) {
        Vec3d direction = camera.getRotationVec(tickDelta).normalize();
        Vec3d start = camera.getCameraPosVec(tickDelta).add(direction.multiply(1.0));
        Vec3d end = start.add(direction.multiply(range));
        return camera.getEntityWorld().raycast(new RaycastContext(start, end, RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, camera));
    }

    // raycast for living entities
    public static EntityHitResult raycastLivingEntity(Entity camera, double range, float tickDelta) {
        Vec3d start = camera.getCameraPosVec(tickDelta);
        Vec3d direction = camera.getRotationVec(tickDelta).normalize();
        World world = camera.getEntityWorld();
        Vec3d current = start;
        while (current.distanceTo(start) < range) {
            Box box = new Box(current.subtract(0.5, 0.5, 0.5), current.add(0.5, 0.5, 0.5));
            List<LivingEntity> entities = world.getEntitiesByClass(LivingEntity.class, box, e -> e != camera && !e.isSpectator() && e.canHit());
            if (!entities.isEmpty()) {
                return new EntityHitResult(entities.getFirst(), current);
            }
            current = current.add(direction.multiply(ENTITY_RAYCAST_STEP));
        }
        return null;
    }

    // spawns particles in a line from start to end positions
    public void spawnParticleLine(ServerWorld world, Vec3d start, Vec3d end, ParticleEffect pfx) {
        Vec3d direction = end.subtract(start).normalize();
        double distance = start.distanceTo(end);
        Vec3d particlePos = start;
        while (particlePos.distanceTo(start) < distance) {
            world.spawnParticles(pfx, particlePos.x, particlePos.y, particlePos.z, 1, 0.0, 0.0, 0.0, 1.0);
            particlePos = particlePos.add(direction.multiply(PARTICLE_STEP));
        }
    }

    // plays the weapon's firing sound at the player's location
    public void playFireSound(World world, PlayerEntity player, String soundID) {
        world.playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                SoundEvent.of(Identifier.of(soundID)),
                SoundCategory.NEUTRAL,
                1.0F,
                1.0F
        );
    }
}