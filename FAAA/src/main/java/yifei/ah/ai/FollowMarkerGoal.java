package yifei.ah.ai;

import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import yifei.ah.manager.FriendlyMobManager;

import java.util.EnumSet;
import java.util.UUID;

public class FollowMarkerGoal extends Goal {
    private final MobEntity mob;
    private final double speed;
    private Vec3d targetPos;
    private int followTime;

    public FollowMarkerGoal(MobEntity mob, double speed) {
        this.mob = mob;
        this.speed = speed;
        this.setControls(EnumSet.of(Control.MOVE));
    }

    @Override
    public boolean canStart() {
        if (!FriendlyMobManager.isFriendly(mob)) return false;
        
        UUID ownerUUID = FriendlyMobManager.getOwnerUUID(mob);
        if (ownerUUID == null) return false;
        
        PlayerEntity owner = mob.getWorld().getPlayerByUuid(ownerUUID);
        if (owner == null) return false;
        
        targetPos = FriendlyMobManager.getMarkerPosition(owner);
        return targetPos != null && mob.squaredDistanceTo(targetPos) > 1.0;
    }

    @Override
    public void start() {
        followTime = 0;
    }

    @Override
    public boolean shouldContinue() {
        if (!FriendlyMobManager.isFriendly(mob)) return false;
        
        UUID ownerUUID = FriendlyMobManager.getOwnerUUID(mob);
        if (ownerUUID == null) return false;
        
        PlayerEntity owner = mob.getWorld().getPlayerByUuid(ownerUUID);
        if (owner == null) return false;
        
        Vec3d newPos = FriendlyMobManager.getMarkerPosition(owner);
        if (newPos != null) {
            targetPos = newPos;
        }
        
        return targetPos != null && mob.squaredDistanceTo(targetPos) > 1.0 && followTime < 200;
    }

    @Override
    public void tick() {
        if (targetPos == null) return;
        
        mob.getLookControl().lookAt(targetPos.x, targetPos.y, targetPos.z, 30.0F, 30.0F);
        mob.getNavigation().startMovingTo(targetPos.x, targetPos.y, targetPos.z, speed);
        
        followTime++;
    }

    @Override
    public void stop() {
        mob.getNavigation().stop();
        followTime = 0;
    }
}