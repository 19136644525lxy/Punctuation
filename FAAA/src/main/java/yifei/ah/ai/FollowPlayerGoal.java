package yifei.ah.ai;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.GuardianEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MagmaCubeEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.ShulkerEntity;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import yifei.ah.manager.FriendlyMobBehavior;
import yifei.ah.manager.FriendlyMobData;
import yifei.ah.manager.FriendlyMobManager;

import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

public class FollowPlayerGoal extends Goal {
    private final MobEntity mob;
    private PlayerEntity owner;
    private final double speed;
    private final int followRange;
    private final int attackRange;
    private LivingEntity attackTarget;

    public FollowPlayerGoal(MobEntity mob, double speed) {
        this.mob = mob;
        this.speed = speed;
        this.followRange = 64;
        this.attackRange = 64;
        this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
    }

    @Override
    public boolean canStart() {
        if (!FriendlyMobManager.isFriendly(mob)) return false;
        FriendlyMobData data = FriendlyMobManager.getFriendlyMobs().get(mob.getUuid());
        if (data == null || data.getBehavior() != FriendlyMobBehavior.FOLLOW) return false;

        UUID ownerUUID = FriendlyMobManager.getOwnerUUID(mob);
        if (ownerUUID == null) return false;

        owner = mob.getWorld().getPlayerByUuid(ownerUUID);
        return owner != null;
    }

    @Override
    public boolean shouldContinue() {
        if (!FriendlyMobManager.isFriendly(mob)) return false;
        FriendlyMobData data = FriendlyMobManager.getFriendlyMobs().get(mob.getUuid());
        if (data == null || data.getBehavior() != FriendlyMobBehavior.FOLLOW) return false;

        if (owner == null || !owner.isAlive()) {
            UUID ownerUUID = FriendlyMobManager.getOwnerUUID(mob);
            if (ownerUUID != null) {
                owner = mob.getWorld().getPlayerByUuid(ownerUUID);
            }
        }
        return owner != null && owner.isAlive();
    }

    @Override
    public void tick() {
        if (owner == null) return;

        attackTarget = findNearestHostile();
        
        if (attackTarget != null) {
            mob.getLookControl().lookAt(attackTarget, 30.0F, 30.0F);
            double distance = mob.squaredDistanceTo(attackTarget);
            if (distance <= 4.0) {
                mob.getNavigation().stop();
                mob.tryAttack(attackTarget);
            } else {
                mob.getNavigation().startMovingTo(attackTarget, speed);
            }
            return;
        }

        double distanceToOwner = mob.squaredDistanceTo(owner);
        
        if (distanceToOwner > (double)(followRange * followRange)) {
            teleportToOwner();
            return;
        }

        if (distanceToOwner > 9.0) {
            mob.getLookControl().lookAt(owner, 30.0F, 30.0F);
            mob.getNavigation().startMovingTo(owner, speed);
        } else {
            mob.getNavigation().stop();
        }
    }

    private void teleportToOwner() {
        if (owner == null) return;

        Vec3d ownerPos = owner.getPos();
        Vec3d safePos = findSafePosition(ownerPos, 5.0);
        
        if (safePos != null) {
            mob.refreshPositionAndAngles(safePos.x, safePos.y, safePos.z, mob.getYaw(), mob.getPitch());
            mob.getNavigation().stop();
        }
    }

    private Vec3d findSafePosition(Vec3d center, double radius) {
        for (int i = 0; i < 20; i++) {
            double angle = (Math.PI * 2 * i) / 20;
            double x = center.x + radius * Math.cos(angle);
            double z = center.z + radius * Math.sin(angle);
            
            for (int yOffset = 0; yOffset >= -3; yOffset--) {
                double y = center.y + yOffset;
                Vec3d pos = new Vec3d(x, y, z);
                
                if (isSafePosition(pos)) {
                    return pos;
                }
            }
        }
        return center;
    }

    private boolean isSafePosition(Vec3d pos) {
        Box box = new Box(pos.x - 0.5, pos.y, pos.z - 0.5, pos.x + 0.5, pos.y + 2.0, pos.z + 0.5);
        BlockPos blockPos = BlockPos.ofFloored(pos);
        return mob.getWorld().isSpaceEmpty(box) && mob.getWorld().getBlockState(blockPos).isAir()
                && mob.getWorld().getBlockState(blockPos.down()).isSolid();
    }

    private LivingEntity findNearestHostile() {
        Vec3d searchCenter = owner != null ? owner.getPos() : mob.getPos();

        Box searchBox = new Box(
            searchCenter.x - attackRange, searchCenter.y - attackRange, searchCenter.z - attackRange,
            searchCenter.x + attackRange, searchCenter.y + attackRange, searchCenter.z + attackRange
        );

        List<LivingEntity> entities = mob.getWorld().getEntitiesByClass(
            LivingEntity.class,
            searchBox,
            entity -> isHostile(entity) && !entity.isRemoved() && entity != mob && entity.isAlive()
        );

        LivingEntity nearest = null;
        double nearestDistance = Double.MAX_VALUE;

        for (LivingEntity entity : entities) {
            if (!FriendlyMobManager.shouldAttack(mob, entity)) continue;
            double distance = mob.squaredDistanceTo(entity);
            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearest = entity;
            }
        }

        return nearest;
    }

    private boolean isHostile(LivingEntity entity) {
        if (!(entity instanceof MobEntity)) return false;
        MobEntity mobEntity = (MobEntity) entity;
        if (mobEntity instanceof HostileEntity) return true;
        if (mobEntity instanceof SlimeEntity) return true;
        if (mobEntity instanceof MagmaCubeEntity) return true;
        if (mobEntity instanceof ShulkerEntity) return true;
        if (mobEntity instanceof GuardianEntity) return true;
        return false;
    }
}