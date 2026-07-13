package yifei.pua.api.impl;

import org.joml.Quaternionf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.*;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import yifei.pua.api.RenderAPI;
import yifei.pua.config.PunctuationConfig;

public class RenderAPIImpl implements RenderAPI {
    private static final Logger LOGGER = LoggerFactory.getLogger(RenderAPIImpl.class);
    private static final float A = 1.0f;

    private static final Identifier MARKER_TEXTURE = new Identifier("pua", "textures/gui/punctuation.png");

    @Override
    public void renderBlockMarker(MatrixStack matrices, BlockPos pos) {
        MinecraftClient client = MinecraftClient.getInstance();
        VertexConsumerProvider vertexConsumers = client.getBufferBuilders().getEntityVertexConsumers();

        Vec3d cameraPos = client.gameRenderer.getCamera().getPos();
        matrices.push();
        matrices.translate(pos.getX() + 0.5 - cameraPos.x, pos.getY() + 0.5 - cameraPos.y, pos.getZ() + 0.5 - cameraPos.z);

        renderBlockHighlight(matrices, vertexConsumers);
        renderMarkerIcon(matrices, vertexConsumers);

        matrices.pop();

        ((VertexConsumerProvider.Immediate) vertexConsumers).draw();
    }

    @Override
    public void renderEntityMarker(MatrixStack matrices, Vec3d entityPos, Vec3d playerEyePos, ItemStack itemStack) {
        MinecraftClient client = MinecraftClient.getInstance();
        VertexConsumerProvider vertexConsumers = client.getBufferBuilders().getEntityVertexConsumers();

        Vec3d cameraPos = client.gameRenderer.getCamera().getPos();
        Vec3d direction = entityPos.subtract(playerEyePos);
        double distance = direction.length();

        matrices.push();
        matrices.translate(playerEyePos.x - cameraPos.x, playerEyePos.y - cameraPos.y, playerEyePos.z - cameraPos.z);

        float yaw = (float) Math.atan2(direction.z, direction.x);
        float pitch = (float) Math.asin(-direction.y / distance);

        matrices.multiply(new Quaternionf().rotationY(yaw));
        matrices.multiply(new Quaternionf().rotationX(pitch));

        float beamRadius = 0.05f;
        float beamHeight = (float) distance;

        matrices.push();
        matrices.multiply(new Quaternionf().rotationY((float) (Math.PI / 4)));
        renderBeamPart(matrices, vertexConsumers.getBuffer(RenderLayer.getLines()), beamHeight, beamRadius);
        matrices.pop();

        matrices.multiply(new Quaternionf().rotationY((float) (Math.PI / 2)));
        renderBeamPart(matrices, vertexConsumers.getBuffer(RenderLayer.getLines()), beamHeight, beamRadius);

        matrices.pop();

        matrices.push();
        matrices.translate(entityPos.x - cameraPos.x, entityPos.y + 0.5 - cameraPos.y, entityPos.z - cameraPos.z);
        renderItemIcon(matrices, vertexConsumers, itemStack);
        matrices.pop();

        ((VertexConsumerProvider.Immediate) vertexConsumers).draw();
    }

    private void renderBlockHighlight(MatrixStack matrices, VertexConsumerProvider vertexConsumers) {
        if (!PunctuationConfig.showBorder) return;

        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderLayer.getLines());
        MatrixStack.Entry entry = matrices.peek();

        float offset = 0.005f;
        float x0 = -0.5f - offset, x1 = 0.5f + offset;
        float y0 = -0.5f - offset, y1 = 0.5f + offset;
        float z0 = -0.5f - offset, z1 = 0.5f + offset;

        float nx = 0, ny = 0, nz = 1;

        float[] colorStart = PunctuationConfig.getBorderColor();
        float[] colorEnd = PunctuationConfig.getBorderColorEnd();
        boolean useGradient = PunctuationConfig.useGradient;

        renderLine(vertexConsumer, entry, x0, y0, z1, x1, y0, z1, nx, ny, nz, colorStart, colorEnd, useGradient);
        renderLine(vertexConsumer, entry, x1, y0, z1, x1, y1, z1, nx, ny, nz, colorStart, colorEnd, useGradient);
        renderLine(vertexConsumer, entry, x1, y1, z1, x0, y1, z1, nx, ny, nz, colorStart, colorEnd, useGradient);
        renderLine(vertexConsumer, entry, x0, y1, z1, x0, y0, z1, nx, ny, nz, colorStart, colorEnd, useGradient);

        renderLine(vertexConsumer, entry, x0, y0, z0, x1, y0, z0, nx, ny, -nz, colorStart, colorEnd, useGradient);
        renderLine(vertexConsumer, entry, x1, y0, z0, x1, y1, z0, nx, ny, -nz, colorStart, colorEnd, useGradient);
        renderLine(vertexConsumer, entry, x1, y1, z0, x0, y1, z0, nx, ny, -nz, colorStart, colorEnd, useGradient);
        renderLine(vertexConsumer, entry, x0, y1, z0, x0, y0, z0, nx, ny, -nz, colorStart, colorEnd, useGradient);

        renderLine(vertexConsumer, entry, x0, y0, z0, x0, y0, z1, -nz, ny, nx, colorStart, colorEnd, useGradient);
        renderLine(vertexConsumer, entry, x1, y0, z0, x1, y0, z1, nz, ny, nx, colorStart, colorEnd, useGradient);
        renderLine(vertexConsumer, entry, x0, y1, z0, x0, y1, z1, -nz, ny, nx, colorStart, colorEnd, useGradient);
        renderLine(vertexConsumer, entry, x1, y1, z0, x1, y1, z1, nz, ny, nx, colorStart, colorEnd, useGradient);
    }

    private void renderLine(VertexConsumer vertexConsumer, MatrixStack.Entry entry,
                           float x1, float y1, float z1, float x2, float y2, float z2,
                           float nx, float ny, float nz, float[] colorStart, float[] colorEnd, boolean useGradient) {
        if (useGradient) {
            vertexConsumer.vertex(entry.getPositionMatrix(), x1, y1, z1)
                    .color(colorStart[0], colorStart[1], colorStart[2], A)
                    .normal(entry.getNormalMatrix(), nx, ny, nz)
                    .next();
            vertexConsumer.vertex(entry.getPositionMatrix(), x2, y2, z2)
                    .color(colorEnd[0], colorEnd[1], colorEnd[2], A)
                    .normal(entry.getNormalMatrix(), nx, ny, nz)
                    .next();
        } else {
            vertexConsumer.vertex(entry.getPositionMatrix(), x1, y1, z1)
                    .color(colorStart[0], colorStart[1], colorStart[2], A)
                    .normal(entry.getNormalMatrix(), nx, ny, nz)
                    .next();
            vertexConsumer.vertex(entry.getPositionMatrix(), x2, y2, z2)
                    .color(colorStart[0], colorStart[1], colorStart[2], A)
                    .normal(entry.getNormalMatrix(), nx, ny, nz)
                    .next();
        }
    }

    private static boolean markerTextureLogged = false;

    private void renderMarkerIcon(MatrixStack matrices, VertexConsumerProvider vertexConsumers) {
        if (!markerTextureLogged) {
            markerTextureLogged = true;
            LOGGER.info("renderMarkerIcon 方法被调用");
        }

        MinecraftClient client = MinecraftClient.getInstance();
        Camera camera = client.gameRenderer.getCamera();

        matrices.push();
        matrices.translate(0, 0.7f, 0);
        matrices.multiply(camera.getRotation());
        matrices.scale(1.5f, 1.5f, 1.5f);

        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderLayer.getEntityCutoutNoCull(MARKER_TEXTURE));
        MatrixStack.Entry entry = matrices.peek();

        float size = 1.5f;

        vertexConsumer.vertex(entry.getPositionMatrix(), -size, -size, 0)
                .color(1.0f, 1.0f, 1.0f, 1.0f)
                .texture(0.0f, 1.0f)
                .overlay(OverlayTexture.DEFAULT_UV)
                .light(LightmapTextureManager.MAX_LIGHT_COORDINATE)
                .normal(entry.getNormalMatrix(), 0, 0, 1)
                .next();

        vertexConsumer.vertex(entry.getPositionMatrix(), size, -size, 0)
                .color(1.0f, 1.0f, 1.0f, 1.0f)
                .texture(1.0f, 1.0f)
                .overlay(OverlayTexture.DEFAULT_UV)
                .light(LightmapTextureManager.MAX_LIGHT_COORDINATE)
                .normal(entry.getNormalMatrix(), 0, 0, 1)
                .next();

        vertexConsumer.vertex(entry.getPositionMatrix(), size, size, 0)
                .color(1.0f, 1.0f, 1.0f, 1.0f)
                .texture(1.0f, 0.0f)
                .overlay(OverlayTexture.DEFAULT_UV)
                .light(LightmapTextureManager.MAX_LIGHT_COORDINATE)
                .normal(entry.getNormalMatrix(), 0, 0, 1)
                .next();

        vertexConsumer.vertex(entry.getPositionMatrix(), -size, size, 0)
                .color(1.0f, 1.0f, 1.0f, 1.0f)
                .texture(0.0f, 0.0f)
                .overlay(OverlayTexture.DEFAULT_UV)
                .light(LightmapTextureManager.MAX_LIGHT_COORDINATE)
                .normal(entry.getNormalMatrix(), 0, 0, 1)
                .next();

        matrices.pop();
    }

    private void renderBeamPart(MatrixStack matrices, VertexConsumer vertexConsumer, float height, float radius) {
        MatrixStack.Entry entry = matrices.peek();

        float x0 = -radius, x1 = radius;
        float y0 = 0, y1 = height;
        float z0 = -radius, z1 = radius;

        float nx = 0, ny = 0, nz = 1;

        renderBeamLine(vertexConsumer, entry, x0, y0, z0, x1, y0, z0, nx, ny, nz);
        renderBeamLine(vertexConsumer, entry, x1, y0, z0, x1, y1, z0, nx, ny, nz);
        renderBeamLine(vertexConsumer, entry, x1, y1, z0, x0, y1, z0, nx, ny, nz);
        renderBeamLine(vertexConsumer, entry, x0, y1, z0, x0, y0, z0, nx, ny, nz);

        renderBeamLine(vertexConsumer, entry, x0, y0, z1, x1, y0, z1, nx, ny, -nz);
        renderBeamLine(vertexConsumer, entry, x1, y0, z1, x1, y1, z1, nx, ny, -nz);
        renderBeamLine(vertexConsumer, entry, x1, y1, z1, x0, y1, z1, nx, ny, -nz);
        renderBeamLine(vertexConsumer, entry, x0, y1, z1, x0, y0, z1, nx, ny, -nz);

        renderBeamLine(vertexConsumer, entry, x0, y0, z0, x0, y0, z1, -nz, ny, nx);
        renderBeamLine(vertexConsumer, entry, x0, y0, z1, x0, y1, z1, -nz, ny, nx);
        renderBeamLine(vertexConsumer, entry, x0, y1, z1, x0, y1, z0, -nz, ny, nx);
        renderBeamLine(vertexConsumer, entry, x0, y1, z0, x0, y0, z0, -nz, ny, nx);

        renderBeamLine(vertexConsumer, entry, x1, y0, z0, x1, y0, z1, nz, ny, nx);
        renderBeamLine(vertexConsumer, entry, x1, y0, z1, x1, y1, z1, nz, ny, nx);
        renderBeamLine(vertexConsumer, entry, x1, y1, z1, x1, y1, z0, nz, ny, nx);
        renderBeamLine(vertexConsumer, entry, x1, y1, z0, x1, y0, z0, nz, ny, nx);
    }

    private void renderBeamLine(VertexConsumer vertexConsumer, MatrixStack.Entry entry,
                                float x1, float y1, float z1, float x2, float y2, float z2,
                                float nx, float ny, float nz) {
        vertexConsumer.vertex(entry.getPositionMatrix(), x1, y1, z1)
                .color(0.0f, 0.8f, 1.0f, 1.0f)
                .normal(entry.getNormalMatrix(), nx, ny, nz)
                .next();
        vertexConsumer.vertex(entry.getPositionMatrix(), x2, y2, z2)
                .color(0.0f, 0.8f, 1.0f, 1.0f)
                .normal(entry.getNormalMatrix(), nx, ny, nz)
                .next();
    }

    private void renderItemIcon(MatrixStack matrices, VertexConsumerProvider vertexConsumers, ItemStack itemStack) {
        if (itemStack.isEmpty()) return;

        MinecraftClient client = MinecraftClient.getInstance();
        Camera camera = client.gameRenderer.getCamera();

        matrices.push();
        matrices.multiply(camera.getRotation());
        matrices.scale(0.5f, 0.5f, 0.5f);

        DiffuseLighting.enableGuiDepthLighting();
        ItemRenderer itemRenderer = client.getItemRenderer();
        itemRenderer.renderItem(itemStack, ModelTransformationMode.GUI,
                LightmapTextureManager.MAX_LIGHT_COORDINATE, OverlayTexture.DEFAULT_UV,
                matrices, vertexConsumers, client.world, 0);
        DiffuseLighting.disableGuiDepthLighting();

        TextRenderer textRenderer = client.textRenderer;
        String itemName = itemStack.getName().getString();

        float textWidth = textRenderer.getWidth(itemName);
        float textHeight = textRenderer.fontHeight;

        matrices.push();
        matrices.translate(-textWidth / 2, -textHeight - 10, 0.01f);

        textRenderer.draw(itemName, 0, 0, 0xFFFFFF, false, matrices.peek().getPositionMatrix(), vertexConsumers, TextRenderer.TextLayerType.NORMAL, 0, LightmapTextureManager.MAX_LIGHT_COORDINATE);

        matrices.pop();
        matrices.pop();
    }
}