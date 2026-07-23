package yifei.ah.ai;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.SkeletonEntity;
import net.minecraft.entity.mob.PillagerEntity;
import net.minecraft.entity.mob.EvokerEntity;
import net.minecraft.entity.mob.AbstractSkeletonEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import yifei.ah.manager.FriendlyMobManager;

import java.util.EnumSet;
import java.util.List;

public class RangedAttackGoal extends Goal {
    private final MobEntity mob;
    private LivingEntity target;
    private final double speed;
    private final int attackRange;
    private int attackCooldown;

    public RangedAttackGoal(MobEntity mob, double speed) {
        this.mob = mob;
        this.speed = speed;
        this.attackRange = 32;
        this.attackCooldown = 0;
        this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
    }

    @Override
    public boolean canStart() {
        if (!FriendlyMobManager.isFriendly(mob)) return false;
        if (!(mob instanceof AbstractSkeletonEntity) && !(mob instanceof PillagerEntity) && !(mob instanceof EvokerEntity)) {
            return false;
        }

        target = findNearestHostile();
        return target != null && FriendlyMobManager.shouldAttack(mob, target);
    }

    @Override
    public void start() {
        mob.setAttacking(true);
    }

    @Override
    public boolean shouldContinue() {
        if (!FriendlyMobManager.isFriendly(mob)) return false;
        if (target == null || target.isDead() || !target.isAlive()) return false;
        if (!FriendlyMobManager.shouldAttack(mob, target)) return false;

        return mob.squaredDistanceTo(target) <= (double)(attackRange * attackRange);
    }

    @Override
    public void tick() {
        if (target == null || !target.isAlive()) {
            target = findNearestHostile();
            if (target == null) return;
        }

        mob.getLookControl().lookAt(target, 30.0F, 30.0F);

        double distance = mob.squaredDistanceTo(target);
        double attackDistance = 16.0;

        if (distance > attackDistance) {
            mob.getNavigation().startMovingTo(target, speed);
        } else {
            mob.getNavigation().stop();

            if (attackCooldown <= 0) {
                if (mob instanceof AbstractSkeletonEntity) {
                    AbstractSkeletonEntity skeleton = (AbstractSkeletonEntity) mob;
                    skeleton.attack(target, 1.0F);
                } else if (mob instanceof PillagerEntity) {
                    PillagerEntity pillager = (PillagerEntity) mob;
                    pillager.attack(target, 1.0F);
                } else if (mob instanceof EvokerEntity) {
                    EvokerEntity evoker = (EvokerEntity) mob;
                    evoker.setTarget(target);
                }
                attackCooldown = 20;
            }
        }

        if (attackCooldown > 0) {
            attackCooldown--;
        }
    }

    @Override
    public void stop() {
        mob.setAttacking(false);
        mob.getNavigation().stop();
        target = null;
    }

    private LivingEntity findNearestHostile() {
        Vec3d searchCenter = mob.getPos();

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
        return mobEntity instanceof HostileEntity
            || mobEntity instanceof net.minecraft.entity.mob.SlimeEntity
            || mobEntity instanceof net.minecraft.entity.mob.MagmaCubeEntity
            || mobEntity instanceof net.minecraft.entity.mob.ShulkerEntity
            || mobEntity instanceof net.minecraft.entity.mob.GuardianEntity;
    }
}