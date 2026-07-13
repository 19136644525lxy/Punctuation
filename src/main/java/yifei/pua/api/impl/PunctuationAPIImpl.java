package yifei.pua.api.impl;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import yifei.pua.api.PunctuationAPI;
import yifei.pua.api.RenderAPI;
import yifei.pua.api.ParticleAPI;
import yifei.pua.api.RaycastAPI;

public class PunctuationAPIImpl implements PunctuationAPI {
    private final RenderAPI renderAPI;
    private final ParticleAPI particleAPI;
    private final RaycastAPI raycastAPI;

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
    }

    @Override
    public void setMarkerEntity(Vec3d pos, net.minecraft.item.ItemStack itemStack) {
    }

    @Override
    public void clearMarker() {
    }
}