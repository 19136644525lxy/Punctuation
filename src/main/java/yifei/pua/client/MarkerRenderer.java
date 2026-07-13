package yifei.pua.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.*;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

public class MarkerRenderer {
    private static final Identifier MARKER_TEXTURE = new Identifier("pua", "textures/gui/punctuation.png");

    public static void renderBlockHighlight(MatrixStack matrices, VertexConsumerProvider vertexConsumers) {
        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderLayer.getLines());

        MatrixStack.Entry entry = matrices.peek();

        float r = 0.0f;
        float g = 1.0f;
        float b = 0.5f;
        float a = 1.0f;
        float offset = 0.005f;

        float x0 = -0.5f - offset;
        float x1 = 0.5f + offset;
        float y0 = -0.5f - offset;
        float y1 = 0.5f + offset;
        float z0 = -0.5f - offset;
        float z1 = 0.5f + offset;

        float normalX = 0, normalY = 0, normalZ = 1;

        vertexConsumer.vertex(entry.getPositionMatrix(), x0, y0, z1).color(r, g, b, a).normal(entry.getNormalMatrix(), normalX, normalY, normalZ).next();
        vertexConsumer.vertex(entry.getPositionMatrix(), x1, y0, z1).color(r, g, b, a).normal(entry.getNormalMatrix(), normalX, normalY, normalZ).next();
        vertexConsumer.vertex(entry.getPositionMatrix(), x1, y0, z1).color(r, g, b, a).normal(entry.getNormalMatrix(), normalX, normalY, normalZ).next();
        vertexConsumer.vertex(entry.getPositionMatrix(), x1, y1, z1).color(r, g, b, a).normal(entry.getNormalMatrix(), normalX, normalY, normalZ).next();
        vertexConsumer.vertex(entry.getPositionMatrix(), x1, y1, z1).color(r, g, b, a).normal(entry.getNormalMatrix(), normalX, normalY, normalZ).next();
        vertexConsumer.vertex(entry.getPositionMatrix(), x0, y1, z1).color(r, g, b, a).normal(entry.getNormalMatrix(), normalX, normalY, normalZ).next();
        vertexConsumer.vertex(entry.getPositionMatrix(), x0, y1, z1).color(r, g, b, a).normal(entry.getNormalMatrix(), normalX, normalY, normalZ).next();
        vertexConsumer.vertex(entry.getPositionMatrix(), x0, y0, z1).color(r, g, b, a).normal(entry.getNormalMatrix(), normalX, normalY, normalZ).next();

        vertexConsumer.vertex(entry.getPositionMatrix(), x0, y0, z0).color(r, g, b, a).normal(entry.getNormalMatrix(), normalX, normalY, -normalZ).next();
        vertexConsumer.vertex(entry.getPositionMatrix(), x1, y0, z0).color(r, g, b, a).normal(entry.getNormalMatrix(), normalX, normalY, -normalZ).next();
        vertexConsumer.vertex(entry.getPositionMatrix(), x1, y0, z0).color(r, g, b, a).normal(entry.getNormalMatrix(), normalX, normalY, -normalZ).next();
        vertexConsumer.vertex(entry.getPositionMatrix(), x1, y1, z0).color(r, g, b, a).normal(entry.getNormalMatrix(), normalX, normalY, -normalZ).next();
        vertexConsumer.vertex(entry.getPositionMatrix(), x1, y1, z0).color(r, g, b, a).normal(entry.getNormalMatrix(), normalX, normalY, -normalZ).next();
        vertexConsumer.vertex(entry.getPositionMatrix(), x0, y1, z0).color(r, g, b, a).normal(entry.getNormalMatrix(), normalX, normalY, -normalZ).next();
        vertexConsumer.vertex(entry.getPositionMatrix(), x0, y1, z0).color(r, g, b, a).normal(entry.getNormalMatrix(), normalX, normalY, -normalZ).next();
        vertexConsumer.vertex(entry.getPositionMatrix(), x0, y0, z0).color(r, g, b, a).normal(entry.getNormalMatrix(), normalX, normalY, -normalZ).next();

        vertexConsumer.vertex(entry.getPositionMatrix(), x0, y0, z0).color(r, g, b, a).normal(entry.getNormalMatrix(), -normalZ, normalY, normalX).next();
        vertexConsumer.vertex(entry.getPositionMatrix(), x0, y0, z1).color(r, g, b, a).normal(entry.getNormalMatrix(), -normalZ, normalY, normalX).next();
        vertexConsumer.vertex(entry.getPositionMatrix(), x1, y0, z0).color(r, g, b, a).normal(entry.getNormalMatrix(), normalZ, normalY, normalX).next();
        vertexConsumer.vertex(entry.getPositionMatrix(), x1, y0, z1).color(r, g, b, a).normal(entry.getNormalMatrix(), normalZ, normalY, normalX).next();
        vertexConsumer.vertex(entry.getPositionMatrix(), x0, y1, z0).color(r, g, b, a).normal(entry.getNormalMatrix(), -normalZ, normalY, normalX).next();
        vertexConsumer.vertex(entry.getPositionMatrix(), x0, y1, z1).color(r, g, b, a).normal(entry.getNormalMatrix(), -normalZ, normalY, normalX).next();
        vertexConsumer.vertex(entry.getPositionMatrix(), x1, y1, z0).color(r, g, b, a).normal(entry.getNormalMatrix(), normalZ, normalY, normalX).next();
        vertexConsumer.vertex(entry.getPositionMatrix(), x1, y1, z1).color(r, g, b, a).normal(entry.getNormalMatrix(), normalZ, normalY, normalX).next();
    }

    public static void renderMarkerIcon(MatrixStack matrices, VertexConsumerProvider vertexConsumers) {
        MinecraftClient client = MinecraftClient.getInstance();
        Camera camera = client.gameRenderer.getCamera();

        matrices.push();
        matrices.translate(0, 0.55f, 0);
        matrices.multiply(camera.getRotation());
        matrices.scale(0.5f, 0.5f, 0.5f);

        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderLayer.getEntityCutout(MARKER_TEXTURE));
        MatrixStack.Entry entry = matrices.peek();

        float size = 0.5f;

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

    public static void renderBeam(MatrixStack matrices, VertexConsumerProvider vertexConsumers, Vec3d direction) {
        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderLayer.getLines());

        MatrixStack.Entry entry = matrices.peek();

        float r = 0.0f;
        float g = 0.8f;
        float b = 1.0f;
        float a = 1.0f;

        vertexConsumer.vertex(entry.getPositionMatrix(), 0, 0, 0)
                .color(r, g, b, a)
                .normal(entry.getNormalMatrix(), 0, 0, 1)
                .next();

        vertexConsumer.vertex(entry.getPositionMatrix(), (float) direction.x, (float) direction.y, (float) direction.z)
                .color(r, g, b, a)
                .normal(entry.getNormalMatrix(), 0, 0, 1)
                .next();
    }

    public static void renderItemIcon(MatrixStack matrices, VertexConsumerProvider vertexConsumers, ItemStack itemStack) {
        if (itemStack.isEmpty()) return;

        MinecraftClient client = MinecraftClient.getInstance();
        Camera camera = client.gameRenderer.getCamera();

        matrices.push();
        matrices.multiply(camera.getRotation());
        matrices.scale(0.5f, 0.5f, 0.5f);

        ItemRenderer itemRenderer = client.getItemRenderer();
        itemRenderer.renderItem(itemStack, ModelTransformationMode.GUI, LightmapTextureManager.MAX_LIGHT_COORDINATE,
                OverlayTexture.DEFAULT_UV, matrices, vertexConsumers, client.world, 0);

        TextRenderer textRenderer = client.textRenderer;
        String itemName = itemStack.getName().getString();

        float textWidth = textRenderer.getWidth(itemName);
        float textHeight = textRenderer.fontHeight;

        matrices.push();
        matrices.translate(-textWidth / 2, -textHeight - 10, 0.01f);

        int color = 0xFFFFFF;
        textRenderer.draw(itemName, 0, 0, color, false, matrices.peek().getPositionMatrix(), vertexConsumers, TextRenderer.TextLayerType.NORMAL, 0, LightmapTextureManager.MAX_LIGHT_COORDINATE);

        matrices.pop();
        matrices.pop();
    }
}