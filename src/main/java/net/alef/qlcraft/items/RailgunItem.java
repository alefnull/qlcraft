package net.alef.qlcraft.items;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.WindChargeEntity;
import net.minecraft.particle.DustColorTransitionParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.stat.Stats;
import net.minecraft.util.Colors;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class RailgunItem extends BaseWeaponItem {
    private static final float POWER = 100.0F;
    private static final double RANGE = 100.0;
    private static final double SECONDARY_RANGE = 2.0;

    public RailgunItem(Settings settings) {
        super(settings);
        INSTAGIB = true;
    }

    @Override
    protected void onPrimaryFire(PlayerEntity player, boolean instagib) {
        World world = player.getEntityWorld();
        ParticleEffect pfx = new DustColorTransitionParticleEffect(Colors.GREEN, Colors.YELLOW, 1.0f);
        playFireSound(world, player, "qlcraft:rail_fire");
        EntityHitResult entityHit = raycastLivingEntity(player, RANGE, 0.0F);
        if (entityHit != null) {
            handleEntityHit(world, player, entityHit, instagib);
        }
        BlockHitResult blockHit = raycastBlock(player, RANGE, 0.0F);
        if (blockHit.getType() != BlockHitResult.Type.MISS) {
            handleBlockHit(world, player, blockHit);
        }
        Vec3d start = player.getCameraPosVec(0.0F);
        Vec3d end = start.add(player.getRotationVec(0.0F).normalize().multiply(RANGE));
        spawnParticleLine((ServerWorld) world, start, end, pfx);
    }

    @Override
    protected void onSecondaryFire(World world, PlayerEntity player, Hand hand, boolean instagib) {
        BlockHitResult blockHit = raycastBlock(player, SECONDARY_RANGE, 0.0F);
        if (blockHit.getType() != BlockHitResult.Type.MISS) {
            spawnWindCharge(world, player, blockHit.getPos());
        }
    }

    // handles hitting an entity: deals damage
    public void handleEntityHit(World world, PlayerEntity player, EntityHitResult entityHit, boolean instagib) {
        Entity target = entityHit.getEntity();
        if (target != null) {
            if (instagib) {
                target.damage(((ServerWorld) world), player.getDamageSources().genericKill(), Float.MAX_VALUE);
            } else {
                target.damage(((ServerWorld) world), player.getDamageSources().genericKill(), POWER);
            }
            player.incrementStat(Stats.USED.getOrCreateStat(this));
        }
    }

    // handles hitting a block: breaks the block if possible
    private void handleBlockHit(World world, PlayerEntity player, BlockHitResult blockHit) {
        if (world.getBlockState(blockHit.getBlockPos()).getHardness(world, blockHit.getBlockPos()) >= 0) {
            world.breakBlock(blockHit.getBlockPos(), true, player);
        }
    }

    // spawns a WindChargeEntity towards the target position
    // (used for railgun secondary fire)
    public void spawnWindCharge(World world, PlayerEntity player, Vec3d targetPos) {
        Vec3d direction = targetPos.subtract(player.getCameraPosVec(0.0F)).normalize();
        WindChargeEntity windCharge = new WindChargeEntity(player, world, 0, 0, 0);
        windCharge.setVelocity(direction.x * 2.0, direction.y * 2.0, direction.z * 2.0);
        windCharge.updatePosition(player.getX(), player.getY() + player.getStandingEyeHeight(), player.getZ());
        world.spawnEntity(windCharge);
    }

}
