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
import yifei.ah.manager.FriendlyMobManager;

import java.util.EnumSet;
import java.util.List;

public class AttackHostileGoal extends Goal {
    private final MobEntity mob;
    private LivingEntity target;
    private final double speed;
    private final int attackRange;

    public AttackHostileGoal(MobEntity mob, double speed) {
        this.mob = mob;
        this.speed = speed;
        this.attackRange = 32;
        this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
    }

    @Override
    public boolean canStart() {
        if (!FriendlyMobManager.isFriendly(mob)) return false;
        
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
        if (distance <= 4.0) {
            mob.getNavigation().stop();
            mob.tryAttack(target);
        } else {
            mob.getNavigation().startMovingTo(target, speed);
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