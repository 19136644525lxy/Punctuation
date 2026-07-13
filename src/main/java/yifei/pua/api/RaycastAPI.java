package yifei.pua.api;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;

public interface RaycastAPI {

    HitResult raycast(PlayerEntity player, double maxDistance);

    BlockHitResult raycastBlock(PlayerEntity player, double maxDistance);

    EntityHitResult raycastEntity(PlayerEntity player, double maxDistance);

    Entity findEntityInLineOfSight(PlayerEntity player, double maxDistance);
}