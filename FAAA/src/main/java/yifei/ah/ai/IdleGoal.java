package yifei.ah.ai;

import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.player.PlayerEntity;
import yifei.ah.manager.FriendlyMobBehavior;
import yifei.ah.manager.FriendlyMobData;
import yifei.ah.manager.FriendlyMobManager;

import java.util.EnumSet;
import java.util.UUID;

public class IdleGoal extends Goal {
    private final MobEntity mob;

    public IdleGoal(MobEntity mob) {
        this.mob = mob;
        this.setControls(EnumSet.of(Control.MOVE));
    }

    @Override
    public boolean canStart() {
        if (!FriendlyMobManager.isFriendly(mob)) return false;
        FriendlyMobData data = FriendlyMobManager.getFriendlyMobs().get(mob.getUuid());
        return data != null && data.getBehavior() == FriendlyMobBehavior.IDLE;
    }

    @Override
    public boolean shouldContinue() {
        return canStart();
    }

    @Override
    public void tick() {
        if (mob.getNavigation().isIdle()) {
            UUID ownerUUID = FriendlyMobManager.getOwnerUUID(mob);
            if (ownerUUID != null) {
                PlayerEntity owner = mob.getWorld().getPlayerByUuid(ownerUUID);
                if (owner != null && owner.isAlive()) {
                    mob.getLookControl().lookAt(owner, 30.0F, 30.0F);
                }
            }
        }
    }
}