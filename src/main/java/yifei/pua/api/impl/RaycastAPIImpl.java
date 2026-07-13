package yifei.pua.api.impl;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import yifei.pua.api.RaycastAPI;

import java.util.Optional;

public class RaycastAPIImpl implements RaycastAPI {

    @Override
    public HitResult raycast(PlayerEntity player, double maxDistance) {
        HitResult blockResult = raycastBlock(player, maxDistance);
        if (blockResult != null && blockResult.getType() != HitResult.Type.MISS) {
            return blockResult;
        }

        EntityHitResult entityResult = raycastEntity(player, maxDistance);
        if (entityResult != null) {
            return entityResult;
        }

        return null;
    }

    @Override
    public BlockHitResult raycastBlock(PlayerEntity player, double maxDistance) {
        if (player.getWorld() == null) return null;

        Vec3d start = player.getEyePos();
        Vec3d look = player.getRotationVector();
        Vec3d end = start.add(look.multiply(maxDistance));

        return player.getWorld().raycast(new RaycastContext(
                start,
                end,
                RaycastContext.ShapeType.OUTLINE,
                RaycastContext.FluidHandling.NONE,
                player
        ));
    }

    @Override
    public EntityHitResult raycastEntity(PlayerEntity player, double maxDistance) {
        if (player.getWorld() == null) return null;

        Vec3d start = player.getEyePos();
        Vec3d look = player.getRotationVector();
        Vec3d end = start.add(look.multiply(maxDistance));

        double closestDistance = maxDistance;
        Entity closestEntity = null;
        Vec3d hitPos = null;

        Box searchBox = new Box(start, end).expand(1.0);
        for (Entity entity : player.getWorld().getOtherEntities(player, searchBox)) {
            if (entity.isRemoved()) continue;

            Box entityBox = entity.getBoundingBox().expand(entity.getTargetingMargin());
            Optional<Vec3d> result = entityBox.raycast(start, end);

            if (result.isPresent()) {
                Vec3d pos = result.get();
                double distance = start.distanceTo(pos);
                if (distance < closestDistance) {
                    closestDistance = distance;
                    closestEntity = entity;
                    hitPos = pos;
                }
            }
        }

        if (closestEntity != null) {
            return new EntityHitResult(closestEntity, hitPos);
        }

        return null;
    }

    @Override
    public Entity findEntityInLineOfSight(PlayerEntity player, double maxDistance) {
        EntityHitResult result = raycastEntity(player, maxDistance);
        return result != null ? result.getEntity() : null;
    }
}