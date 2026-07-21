package yifei.ah.manager;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yifei.ah.FormingAnArmyAlone;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class FriendlyMobManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(FriendlyMobManager.class);
    private static final ConcurrentHashMap<UUID, FriendlyMobData> friendlyMobs = new ConcurrentHashMap<>();

    public static void init() {
        ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
            if (entity instanceof MobEntity) {
                MobEntity mob = (MobEntity) entity;
                if (isFriendly(mob)) {
                    FriendlyMobData data = friendlyMobs.get(mob.getUuid());
                    if (data != null) {
                        data.setEntity(mob);
                        applyBuffEffects(mob);
                    }
                }
            }
        });

        ServerEntityEvents.ENTITY_UNLOAD.register((entity, world) -> {
            if (entity instanceof MobEntity) {
                UUID uuid = entity.getUuid();
                if (friendlyMobs.containsKey(uuid)) {
                    FriendlyMobData data = friendlyMobs.get(uuid);
                    data.setEntity(null);
                }
            }
        });
    }

    public static void makeFriendly(Entity entity, PlayerEntity owner) {
        if (!(entity instanceof MobEntity)) return;

        MobEntity mob = (MobEntity) entity;

        if (isFriendly(mob)) {
            owner.sendMessage(Text.translatable("message.ah.already_friendly"), true);
            return;
        }

        FriendlyMobData data = new FriendlyMobData(mob, owner.getUuid());
        friendlyMobs.put(mob.getUuid(), data);

        mob.setPersistent();
        mob.setTarget(null);

        applyBuffEffects(mob);

        owner.sendMessage(Text.translatable("message.ah.made_friendly", mob.getName().getString()), true);
        FormingAnArmyAlone.LOGGER.info("Made mob friendly: {} owned by {}", mob.getUuid(), owner.getUuid());
    }

    private static void applyBuffEffects(MobEntity mob) {
        net.minecraft.entity.attribute.EntityAttributeInstance healthAttribute = mob.getAttributeInstance(
            net.minecraft.entity.attribute.EntityAttributes.GENERIC_MAX_HEALTH
        );
        if (healthAttribute != null) {
            healthAttribute.setBaseValue(1000.0);
        }
        mob.setHealth(1000.0F);

        net.minecraft.entity.effect.StatusEffectInstance strength = new net.minecraft.entity.effect.StatusEffectInstance(
            net.minecraft.entity.effect.StatusEffects.STRENGTH, Integer.MAX_VALUE, 3, false, false, false
        );
        mob.addStatusEffect(strength);

        net.minecraft.entity.effect.StatusEffectInstance resistance = new net.minecraft.entity.effect.StatusEffectInstance(
            net.minecraft.entity.effect.StatusEffects.RESISTANCE, Integer.MAX_VALUE, 1, false, false, false
        );
        mob.addStatusEffect(resistance);

        if (isUndead(mob)) {
            net.minecraft.entity.effect.StatusEffectInstance undeadStrength = new net.minecraft.entity.effect.StatusEffectInstance(
                net.minecraft.entity.effect.StatusEffects.STRENGTH, Integer.MAX_VALUE, 5, false, false, false
            );
            mob.addStatusEffect(undeadStrength);
        }
    }

    private static boolean isUndead(MobEntity mob) {
        return mob instanceof net.minecraft.entity.mob.ZombieEntity || mob instanceof net.minecraft.entity.mob.AbstractSkeletonEntity;
    }

    public static boolean isFriendly(MobEntity mob) {
        return friendlyMobs.containsKey(mob.getUuid());
    }

    public static UUID getOwnerUUID(MobEntity mob) {
        FriendlyMobData data = friendlyMobs.get(mob.getUuid());
        return data != null ? data.getOwnerUUID() : null;
    }

    public static void removeFriendly(MobEntity mob) {
        friendlyMobs.remove(mob.getUuid());
    }

    public static boolean shouldAttack(MobEntity mob, LivingEntity target) {
        if (!isFriendly(mob)) return false;
        if (target instanceof PlayerEntity) {
            UUID ownerUUID = getOwnerUUID(mob);
            if (ownerUUID != null && ownerUUID.equals(target.getUuid())) {
                return false;
            }
        }
        if (target instanceof MobEntity && isFriendly((MobEntity) target)) {
            return false;
        }
        return true;
    }

    public static Vec3d getMarkerPosition(PlayerEntity player) {
        if (player == null) return null;
        return yifei.pua.api.PunctuationAPIAccess.getInstance().getMarkerPosition() != null
            ? new Vec3d(
                yifei.pua.api.PunctuationAPIAccess.getInstance().getMarkerPosition().getX() + 0.5,
                yifei.pua.api.PunctuationAPIAccess.getInstance().getMarkerPosition().getY(),
                yifei.pua.api.PunctuationAPIAccess.getInstance().getMarkerPosition().getZ() + 0.5
            )
            : yifei.pua.api.PunctuationAPIAccess.getInstance().getMarkerEntityPos();
    }

    public static ConcurrentHashMap<UUID, FriendlyMobData> getFriendlyMobs() {
        return friendlyMobs;
    }

    public static FriendlyMobBehavior getBehavior(MobEntity mob) {
        FriendlyMobData data = friendlyMobs.get(mob.getUuid());
        return data != null ? data.getBehavior() : FriendlyMobBehavior.IDLE;
    }

    public static void cycleBehavior(MobEntity mob) {
        FriendlyMobData data = friendlyMobs.get(mob.getUuid());
        if (data != null) {
            data.cycleBehavior();
            FormingAnArmyAlone.LOGGER.info("Cycled behavior for {}: {}", mob.getUuid(), data.getBehavior());
        }
    }
}