package net.alef.qlcraft.items;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.WindChargeEntity;
import net.minecraft.item.Item;
import net.minecraft.particle.DustColorTransitionParticleEffect;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.stat.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Colors;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

import java.util.List;

public class RailgunItem extends Item {
    private static final float POWER = 100.0F;
    private static final double RANGE = 100.0;
    private static final double PARTICLE_STEP = 0.5;
    private static final double ENTITY_RAYCAST_STEP = 0.25;

    public RailgunItem(Settings settings) {
        super(settings);
    }

    /**
     * Fires the railgun: damages entities or breaks blocks, spawns particles, and plays sound.
     */
    public void fire(PlayerEntity player) {
        if (player.getItemCooldownManager().isCoolingDown(player.getMainHandStack())) {
            return;
        }
        World world = player.getEntityWorld();
        if (!world.isClient()) {
            playFireSound(world, player);
            EntityHitResult entityHit = raycastLivingEntity(player, RANGE, 0.0F);
            if (entityHit != null) {
                handleEntityHit(world, player, entityHit);
            } else {
                BlockHitResult blockHit = raycastBlock(player, RANGE, 0.0F);
                if (blockHit.getType() != BlockHitResult.Type.MISS) {
                    handleBlockHit(world, player, blockHit);
                }
            }
        }
    }

    @Override
    public ActionResult use(World world, PlayerEntity player, Hand hand) {
        if (!world.isClient()) {
            if (player.getItemCooldownManager().isCoolingDown(player.getMainHandStack())) {
                return ActionResult.PASS;
            }
            EntityHitResult entityHit = raycastLivingEntity(player, RANGE, 0.0F);
            if (entityHit != null) {
                spawnWindCharge(world, player, entityHit.getPos());
                return ActionResult.SUCCESS;
            }
            BlockHitResult blockHit = raycastBlock(player, RANGE, 0.0F);
            if (blockHit.getType() != BlockHitResult.Type.MISS) {
                spawnWindCharge(world, player, blockHit.getPos());
                return ActionResult.SUCCESS;
            }
        }
        return ActionResult.SUCCESS;
    }

    /**
     * Raycasts for blocks in a straight line from the camera.
     */
    public static BlockHitResult raycastBlock(Entity camera, double range, float tickDelta) {
        Vec3d direction = camera.getRotationVec(tickDelta).normalize();
        Vec3d start = camera.getCameraPosVec(tickDelta).add(direction.multiply(1.0));
        Vec3d end = start.add(direction.multiply(range));
        return camera.getEntityWorld().raycast(new RaycastContext(start, end, RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, camera));
    }

    /**
     * Raycasts for the first LivingEntity in a straight line, ignoring blocks.
     */
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

    /**
     * Spawns a wind charge projectile towards the target position.
     */
    private void spawnWindCharge(World world, PlayerEntity player, Vec3d targetPos) {
        Vec3d direction = targetPos.subtract(player.getCameraPosVec(0.0F)).normalize();
        WindChargeEntity windCharge = new WindChargeEntity(player, world, 0, 0, 0);
        windCharge.setVelocity(direction.x * 2.0, direction.y * 2.0, direction.z * 2.0);
        windCharge.updatePosition(player.getX(), player.getY() + player.getStandingEyeHeight(), player.getZ());
        world.spawnEntity(windCharge);
    }

    /**
     * Handles hitting a living entity: deals damage, spawns particles, and updates stats.
     */
    private void handleEntityHit(World world, PlayerEntity player, EntityHitResult entityHit) {
        Entity target = entityHit.getEntity();
        if (target != null) {
            target.damage(((ServerWorld) world), player.getDamageSources().genericKill(), POWER);
            player.incrementStat(Stats.USED.getOrCreateStat(this));
            spawnParticleLine((ServerWorld) world, player.getCameraPosVec(0.0F), entityHit.getPos());
        }
    }

    /**
     * Handles hitting a block: breaks block and spawns particles.
     */
    private void handleBlockHit(World world, PlayerEntity player, BlockHitResult blockHit) {
        Vec3d start = player.getCameraPosVec(0.0F);
        Vec3d end = blockHit.getPos();
        spawnParticleLine((ServerWorld) world, start, end);
        if (world.getBlockState(blockHit.getBlockPos()).getHardness(world, blockHit.getBlockPos()) >= 0) {
            world.breakBlock(blockHit.getBlockPos(), true, player);
        }
    }

    /**
     * Spawns a line of particles between two points.
     */
    private void spawnParticleLine(ServerWorld world, Vec3d start, Vec3d end) {
        Vec3d direction = end.subtract(start).normalize();
        double distance = start.distanceTo(end);
        Vec3d particlePos = start;
        while (particlePos.distanceTo(start) < distance) {
            world.spawnParticles(new DustColorTransitionParticleEffect(Colors.GREEN, Colors.YELLOW, 1.0f), particlePos.x, particlePos.y, particlePos.z, 1, 0.0, 0.0, 0.0, 1.0);
            particlePos = particlePos.add(direction.multiply(PARTICLE_STEP));
        }
    }

    /**
     * Plays the railgun firing sound.
     */
    private void playFireSound(World world, PlayerEntity player) {
        world.playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                SoundEvent.of(Identifier.of("qlcraft:rail_fire")),
                SoundCategory.NEUTRAL,
                1.0F,
                1.0F
        );
    }
}
