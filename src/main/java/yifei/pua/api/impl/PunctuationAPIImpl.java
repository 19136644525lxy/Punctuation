package yifei.pua.api.impl;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import yifei.pua.api.MarkerEvent;
import yifei.pua.api.PunctuationAPI;
import yifei.pua.api.RenderAPI;
import yifei.pua.api.ParticleAPI;
import yifei.pua.api.RaycastAPI;
import yifei.pua.api.PunctuationAPIAccess;
import yifei.pua.client.PunctuationClient;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public class PunctuationAPIImpl implements PunctuationAPI {
    private final RenderAPI renderAPI;
    private final ParticleAPI particleAPI;
    private final RaycastAPI raycastAPI;
    private final List<Consumer<MarkerEvent>> markerEventListeners = new ArrayList<>();

    public PunctuationAPIImpl() {
        this.renderAPI = new RenderAPIImpl();
        this.particleAPI = new ParticleAPIImpl();
        this.raycastAPI = new RaycastAPIImpl();
    }

    @Override
    public RenderAPI getRenderAPI() {
        return renderAPI;
    }

    @Override
    public ParticleAPI getParticleAPI() {
        return particleAPI;
    }

    @Override
    public RaycastAPI getRaycastAPI() {
        return raycastAPI;
    }

    @Override
    public void setMarkerPosition(BlockPos pos) {
        PunctuationClient.markerPos = pos;
        PunctuationClient.markerEntityId = null;
        PunctuationClient.markerItemStack = ItemStack.EMPTY;
        PunctuationClient.markerEntityPos = null;
        PunctuationClient.markerType = PunctuationClient.MarkerType.BLOCK;
        PunctuationClient.markerTime = System.currentTimeMillis();
        PunctuationClient.notifiedExpiring = false;
        PunctuationAPIAccess.getParticleAPI().spawnMarkerParticles(new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5));
    }

    @Override
    public void setMarkerEntity(Vec3d pos, ItemStack itemStack) {
        PunctuationClient.markerPos = null;
        PunctuationClient.markerEntityId = null;
        PunctuationClient.markerItemStack = itemStack.copy();
        PunctuationClient.markerEntityPos = pos;
        PunctuationClient.markerType = PunctuationClient.MarkerType.ENTITY;
        PunctuationClient.markerTime = System.currentTimeMillis();
        PunctuationClient.notifiedExpiring = false;
    }

    @Override
    public void clearMarker() {
        PunctuationClient.clearMarker();
    }

    @Override
    public BlockPos getMarkerPosition() {
        return PunctuationClient.markerPos;
    }

    @Override
    public UUID getMarkerEntityId() {
        return PunctuationClient.markerEntityId;
    }

    @Override
    public Vec3d getMarkerEntityPos() {
        return PunctuationClient.markerEntityPos;
    }

    @Override
    public yifei.pua.client.PunctuationClient.MarkerType getMarkerType() {
        return PunctuationClient.markerType;
    }

    @Override
    public ItemStack getMarkerItemStack() {
        return PunctuationClient.markerItemStack;
    }

    @Override
    public Entity getMarkerEntity() {
        if (PunctuationClient.markerEntityId == null) return null;
        if (PunctuationClient.markerEntityPos == null) return null;
        PlayerEntity player = net.minecraft.client.MinecraftClient.getInstance().player;
        if (player == null || player.getWorld() == null) return null;
        
        return player.getWorld().getEntityById(PunctuationClient.markerEntityId.hashCode());
    }

    @Override
    public void onMarkerSet(Consumer<MarkerEvent> callback) {
        markerEventListeners.add(callback);
    }

    public void triggerMarkerEvent(MarkerEvent event) {
        yifei.pua.Punctuation.LOGGER.info("Triggering marker event, listeners count: {}", markerEventListeners.size());
        for (Consumer<MarkerEvent> listener : markerEventListeners) {
            try {
                listener.accept(event);
            } catch (Exception e) {
                yifei.pua.Punctuation.LOGGER.error("Error in marker event listener", e);
            }
        }
    }
}