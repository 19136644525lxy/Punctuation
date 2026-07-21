package yifei.pua.api;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class MarkerEvent {
    private final PlayerEntity player;
    private final Type type;
    private final BlockPos blockPos;
    private final Entity entity;
    private final Vec3d position;

    public MarkerEvent(PlayerEntity player, BlockPos blockPos) {
        this.player = player;
        this.type = Type.BLOCK;
        this.blockPos = blockPos;
        this.entity = null;
        this.position = blockPos != null ? new Vec3d(blockPos.getX() + 0.5, blockPos.getY() + 0.5, blockPos.getZ() + 0.5) : null;
    }

    public MarkerEvent(PlayerEntity player, Entity entity) {
        this.player = player;
        this.type = Type.ENTITY;
        this.blockPos = null;
        this.entity = entity;
        this.position = entity != null ? entity.getPos() : null;
    }

    public PlayerEntity getPlayer() {
        return player;
    }

    public Type getType() {
        return type;
    }

    public BlockPos getBlockPos() {
        return blockPos;
    }

    public Entity getEntity() {
        return entity;
    }

    public Vec3d getPosition() {
        return position;
    }

    public enum Type {
        BLOCK,
        ENTITY
    }
}