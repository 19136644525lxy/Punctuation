package yifei.ah.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.ai.goal.GoalSelector;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yifei.ah.ai.IdleGoal;
import yifei.ah.ai.FollowPlayerGoal;
import yifei.ah.ai.PatrolMarkerGoal;
import yifei.ah.manager.FriendlyMobBehavior;
import yifei.ah.manager.FriendlyMobManager;

import java.util.UUID;

@Mixin(MobEntity.class)
public abstract class MobEntityMixin {

    @Shadow protected GoalSelector goalSelector;
    private boolean goalsAdded = false;

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        MobEntity mob = (MobEntity) (Object) this;

        if (FriendlyMobManager.isFriendly(mob) && !goalsAdded) {
            this.goalSelector.add(1, new IdleGoal(mob));
            this.goalSelector.add(2, new FollowPlayerGoal(mob, 1.0));
            this.goalSelector.add(3, new PatrolMarkerGoal(mob, 1.0));
            goalsAdded = true;
        }
    }

    @Inject(method = "setTarget(Lnet/minecraft/entity/LivingEntity;)V", at = @At("HEAD"), cancellable = true)
    private void onSetTarget(LivingEntity target, CallbackInfo ci) {
        MobEntity mob = (MobEntity) (Object) this;

        if (FriendlyMobManager.isFriendly(mob)) {
            if (target != null && target instanceof PlayerEntity) {
                UUID ownerUUID = FriendlyMobManager.getOwnerUUID(mob);
                if (ownerUUID != null && ownerUUID.equals(target.getUuid())) {
                    ci.cancel();
                }
            }
        }
    }
}