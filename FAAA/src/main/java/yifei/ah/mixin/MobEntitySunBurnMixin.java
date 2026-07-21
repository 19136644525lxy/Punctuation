package yifei.ah.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.AbstractSkeletonEntity;
import net.minecraft.entity.mob.ZombieEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yifei.ah.manager.FriendlyMobManager;

@Mixin(LivingEntity.class)
public abstract class MobEntitySunBurnMixin {

    @Inject(method = "tick", at = @At("HEAD"))
    private void preventFriendlySunBurn(CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        
        if (!(entity instanceof MobEntity)) return;
        
        MobEntity mob = (MobEntity) entity;
        
        if (!FriendlyMobManager.isFriendly(mob)) return;
        
        if (mob instanceof ZombieEntity || mob instanceof AbstractSkeletonEntity) {
            if (entity.isOnFire() && !entity.isTouchingWater()) {
                entity.setFireTicks(0);
                entity.extinguish();
            }
        }
    }
}