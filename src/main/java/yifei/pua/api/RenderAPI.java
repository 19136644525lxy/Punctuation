package yifei.pua.api;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public interface RenderAPI {

    void renderBlockMarker(MatrixStack matrices, BlockPos pos);

    void renderEntityMarker(MatrixStack matrices, Vec3d entityPos, Vec3d playerEyePos, ItemStack itemStack);
}