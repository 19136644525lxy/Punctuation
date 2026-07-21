package yifei.ah.ai;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.GuardianEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MagmaCubeEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.ShulkerEntity;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import yifei.ah.manager.FriendlyMobBehavior;
import yifei.ah.manager.FriendlyMobData;
import yifei.ah.manager.FriendlyMobManager;

import java.util.EnumSet;
import java.util.List;

public class PatrolMarkerGoal extends Goal {
    private final MobEntity mob;
    private final double speed;
    private final int attackRange;
    private Vec3d markerPos;
    private LivingEntity attackTarget;

    public PatrolMarkerGoal(MobEntity mob, double speed) {
        this.mob = mob;
        this.speed = speed;
        this.attackRange = 64;
        this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
    }

    @Override
    public boolean canStart() {
        if (!FriendlyMobManager.isFriendly(mob)) return false;
        FriendlyMobData data = FriendlyMobManager.getFriendlyMobs().get(mob.getUuid());
        if (data == null || data.getBehavior() != FriendlyMobBehavior.PATROL) return false;

        markerPos = FriendlyMobManager.getMarkerPosition(mob.getWorld().getPlayerByUuid(FriendlyMobManager.getOwnerUUID(mob)));
        return markerPos != null;
    }

    @Override
    public boolean shouldContinue() {
        if (!FriendlyMobManager.isFriendly(mob)) return false;
        FriendlyMobData data = FriendlyMobManager.getFriendlyMobs().get(mob.getUuid());
        if (data == null || data.getBehavior() != FriendlyMobBehavior.PATROL) return false;

        return true;
    }

    @Override
    public void tick() {
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

        if (markerPos == null) {
            markerPos = FriendlyMobManager.getMarkerPosition(mob.getWorld().getPlayerByUuid(FriendlyMobManager.getOwnerUUID(mob)));
            if (markerPos == null) return;
        }

        double distanceToMarker = mob.squaredDistanceTo(markerPos);

        if (distanceToMarker > 4.0) {
            mob.getLookControl().lookAt(markerPos.x, markerPos.y, markerPos.z, 30.0F, 30.0F);
            mob.getNavigation().startMovingTo(markerPos.x, markerPos.y, markerPos.z, speed);
        } else {
            mob.getNavigation().stop();
            float wanderAngle = (float) (Math.random() * Math.PI * 2);
            double wanderRadius = 5.0;
            Vec3d wanderPos = new Vec3d(
                markerPos.x + Math.cos(wanderAngle) * wanderRadius,
                markerPos.y,
                markerPos.z + Math.sin(wanderAngle) * wanderRadius
            );
            mob.getLookControl().lookAt(wanderPos.x, wanderPos.y, wanderPos.z, 30.0F, 30.0F);
        }
    }

    private LivingEntity findNearestHostile() {
        if (markerPos == null) return null;

        Box searchBox = new Box(
            markerPos.x - attackRange, markerPos.y - attackRange, markerPos.z - attackRange,
            markerPos.x + attackRange, markerPos.y + attackRange, markerPos.z + attackRange
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