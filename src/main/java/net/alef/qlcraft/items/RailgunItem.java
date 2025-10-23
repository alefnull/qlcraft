package net.alef.qlcraft.items;

import net.alef.qlcraft.QLCraft;
import net.minecraft.entity.LivingEntity;
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
    private static final float POWER = 10.0F;
    private static final double RANGE = 100.0;
    private static final double SECONDARY_RANGE = 2.0;

    public RailgunItem(Settings settings) {
        super(settings);
    }

    //* handles primary fire: raycasts for entity or block hit, deals damage or breaks block
    @Override
    protected void onPrimaryFire(PlayerEntity player) {
        World world = player.getEntityWorld();
        ParticleEffect pfx = new DustColorTransitionParticleEffect(Colors.GREEN, Colors.YELLOW, 1.0f);
        Vec3d particle_start = player.getCameraPosVec(0.0F);
        double particle_range;
        double entityHitDistance = 0.0;
        double blockHitDistance = 0.0;

        EntityHitResult entityHit = raycastLivingEntity(player, RANGE, 0.0F);
        if (entityHit != null) {
            entityHitDistance = entityHit.getPos().distanceTo(particle_start);
            handleEntityHit(world, player, entityHit);
        } else {
            BlockHitResult blockHit = raycastBlock(player, RANGE, 0.0F);
            if (blockHit == null) return;
            blockHitDistance = blockHit.getPos().distanceTo(particle_start);
            if (blockHit.getType() != BlockHitResult.Type.MISS) {
                handleBlockHit(world, player, blockHit);
            }
        }

        particle_range = Math.min(entityHitDistance > 0.0 ? entityHitDistance : RANGE,
                                           blockHitDistance > 0.0 ? blockHitDistance : RANGE);
        Vec3d end = particle_start.add(player.getRotationVec(0.0F).normalize().multiply(particle_range));

        playFireSound(world, player, "qlcraft:rail_fire");
        spawnParticleLine((ServerWorld) world, particle_start, end, pfx);
    }

    //* handles secondary fire: spawns WindChargeEntity towards targeted block
    @Override
    protected void onSecondaryFire(World world, PlayerEntity player, Hand hand) {
        BlockHitResult blockHit = raycastBlock(player, SECONDARY_RANGE, 0.0F);
        if (blockHit.getType() != BlockHitResult.Type.MISS) {
            spawnWindCharge(world, player, blockHit.getPos());
        }
    }

    //* handles hitting an entity: deals damage
    public void handleEntityHit(World world, PlayerEntity player, EntityHitResult entityHit) {
        LivingEntity target = (LivingEntity) entityHit.getEntity();
        if (target != null) {
            if (QLCraft.CONFIG.instagibRailgun()) {
                target.damage(((ServerWorld) world), player.getDamageSources().playerAttack(player), Float.MAX_VALUE);
            } else {
                target.damage(((ServerWorld) world), player.getDamageSources().playerAttack(player), POWER);
            }
            player.incrementStat(Stats.USED.getOrCreateStat(this));
        }
    }

    //* handles hitting a block: breaks the block if possible
    private void handleBlockHit(World world, PlayerEntity player, BlockHitResult blockHit) {
        if (world.getBlockState(blockHit.getBlockPos()).getHardness(world, blockHit.getBlockPos()) >= 0) {
            world.breakBlock(blockHit.getBlockPos(), true, player);
        }
    }

    //* spawns a WindChargeEntity towards the target position
    //* (used for railgun secondary fire)
    //! TO BE RE-WRITTEN WITH THE GOAL OF BEING ABLE TO LAUNCH THE PLAYER HIGHER INTO THE AIR
    public void spawnWindCharge(World world, PlayerEntity player, Vec3d targetPos) {
        Vec3d direction = targetPos.subtract(player.getCameraPosVec(0.0F)).normalize();
        WindChargeEntity windCharge = new WindChargeEntity(player, world, 0, 0, 0);
        windCharge.setVelocity(direction.x * 2.0, direction.y * 2.0, direction.z * 2.0);
        windCharge.updatePosition(player.getX(), player.getY() + player.getStandingEyeHeight(), player.getZ());
        world.spawnEntity(windCharge);
    }
}
