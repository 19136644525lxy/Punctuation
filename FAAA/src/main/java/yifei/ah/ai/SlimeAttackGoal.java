package yifei.ah.ai;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import yifei.ah.manager.FriendlyMobManager;

import java.util.EnumSet;
import java.util.List;

public class SlimeAttackGoal extends Goal {
    private final SlimeEntity slime;
    private LivingEntity target;
    private final double speed;
    private final int attackRange;

    public SlimeAttackGoal(SlimeEntity slime, double speed) {
        this.slime = slime;
        this.speed = speed;
        this.attackRange = 32;
        this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
    }

    @Override
    public boolean canStart() {
        if (!FriendlyMobManager.isFriendly(slime)) return false;

        target = findNearestHostile();
        return target != null && FriendlyMobManager.shouldAttack(slime, target);
    }

    @Override
    public void start() {
        slime.setAttacking(true);
    }

    @Override
    public boolean shouldContinue() {
        if (!FriendlyMobManager.isFriendly(slime)) return false;
        if (target == null || target.isDead() || !target.isAlive()) return false;
        if (!FriendlyMobManager.shouldAttack(slime, target)) return false;

        return slime.squaredDistanceTo(target) <= (double)(attackRange * attackRange);
    }

    @Override
    public void tick() {
        if (target == null || !target.isAlive()) {
            target = findNearestHostile();
            if (target == null) return;
        }

        slime.getLookControl().lookAt(target, 30.0F, 30.0F);

        double distance = slime.squaredDistanceTo(target);
        if (distance <= 4.0) {
            slime.getNavigation().stop();
            slime.setTarget(target);
        } else {
            slime.getNavigation().startMovingTo(target, speed);
        }
    }

    @Override
    public void stop() {
        slime.setAttacking(false);
        slime.getNavigation().stop();
        target = null;
    }

    private LivingEntity findNearestHostile() {
        Vec3d searchCenter = slime.getPos();

        Box searchBox = new Box(
            searchCenter.x - attackRange, searchCenter.y - attackRange, searchCenter.z - attackRange,
            searchCenter.x + attackRange, searchCenter.y + attackRange, searchCenter.z + attackRange
        );

        List<LivingEntity> entities = slime.getWorld().getEntitiesByClass(
            LivingEntity.class,
            searchBox,
            entity -> isHostile(entity) && !entity.isRemoved() && entity != slime && entity.isAlive()
        );

        LivingEntity nearest = null;
        double nearestDistance = Double.MAX_VALUE;

        for (LivingEntity entity : entities) {
            if (!FriendlyMobManager.shouldAttack(slime, entity)) continue;
            double distance = slime.squaredDistanceTo(entity);
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
        return mobEntity instanceof net.minecraft.entity.mob.HostileEntity
            || mobEntity instanceof net.minecraft.entity.mob.SlimeEntity
            || mobEntity instanceof net.minecraft.entity.mob.MagmaCubeEntity
            || mobEntity instanceof net.minecraft.entity.mob.ShulkerEntity
            || mobEntity instanceof net.minecraft.entity.mob.GuardianEntity;
    }
}