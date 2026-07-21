package yifei.pua.api;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import yifei.pua.client.PunctuationClient;

import java.util.UUID;
import java.util.function.Consumer;

public interface PunctuationAPI {

    RenderAPI getRenderAPI();

    ParticleAPI getParticleAPI();

    RaycastAPI getRaycastAPI();

    void setMarkerPosition(BlockPos pos);

    void setMarkerEntity(Vec3d pos, ItemStack itemStack);

    void clearMarker();

    BlockPos getMarkerPosition();

    UUID getMarkerEntityId();

    Vec3d getMarkerEntityPos();

    yifei.pua.client.PunctuationClient.MarkerType getMarkerType();

    ItemStack getMarkerItemStack();

    Entity getMarkerEntity();

    void onMarkerSet(Consumer<MarkerEvent> callback);
}