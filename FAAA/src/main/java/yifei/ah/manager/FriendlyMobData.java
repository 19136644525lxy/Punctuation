package yifei.ah.manager;

import net.minecraft.entity.mob.MobEntity;

import java.util.UUID;

public class FriendlyMobData {
    private MobEntity entity;
    private final UUID ownerUUID;
    private FriendlyMobBehavior behavior;

    public FriendlyMobData(MobEntity entity, UUID ownerUUID) {
        this.entity = entity;
        this.ownerUUID = ownerUUID;
        this.behavior = FriendlyMobBehavior.IDLE;
    }

    public MobEntity getEntity() {
        return entity;
    }

    public void setEntity(MobEntity entity) {
        this.entity = entity;
    }

    public UUID getOwnerUUID() {
        return ownerUUID;
    }

    public FriendlyMobBehavior getBehavior() {
        return behavior;
    }

    public void setBehavior(FriendlyMobBehavior behavior) {
        this.behavior = behavior;
    }

    public void cycleBehavior() {
        this.behavior = this.behavior.next();
    }
}