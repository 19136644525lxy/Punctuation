package yifei.pua.api;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public interface PunctuationAPI {

    RenderAPI getRenderAPI();

    ParticleAPI getParticleAPI();

    RaycastAPI getRaycastAPI();

    void setMarkerPosition(BlockPos pos);

    void setMarkerEntity(Vec3d pos, net.minecraft.item.ItemStack itemStack);

    void clearMarker();
}