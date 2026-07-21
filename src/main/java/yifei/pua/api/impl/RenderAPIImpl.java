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
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import yifei.pua.api.RenderAPI;
import yifei.pua.config.PunctuationConfig;

import java.util.ArrayList;
import java.util.List;

public class RenderAPIImpl implements RenderAPI {
    private static final Logger LOGGER = LoggerFactory.getLogger(RenderAPIImpl.class);
    private static final float A = 1.0f;

    private static final String ICON_PREFIX = "textures/gui/punctuation";
    private static final String ICON_SUFFIX = ".png";
    private static final String NAMESPACE = "pua";

    private static final Identifier[] BUILTIN_ICONS = {
            new Identifier(NAMESPACE, ICON_PREFIX + ICON_SUFFIX),
            new Identifier(NAMESPACE, ICON_PREFIX + "1" + ICON_SUFFIX),
            new Identifier(NAMESPACE, ICON_PREFIX + "2" + ICON_SUFFIX),
            new Identifier(NAMESPACE, ICON_PREFIX + "3" + ICON_SUFFIX)
    };

    private static List<IconInfo> availableIcons = new ArrayList<>();
    private static boolean iconsInitialized = false;

    private static class IconInfo {
        final Identifier texture;
        final String name;
        final boolean isBuiltin;

        IconInfo(Identifier texture, String name, boolean isBuiltin) {
            this.texture = texture;
            this.name = name;
            this.isBuiltin = isBuiltin;
        }
    }

    public static void initializeIcons(ResourceManager resourceManager) {
        if (iconsInitialized) return;
        
        availableIcons.clear();
        
        for (int i = 0; i < BUILTIN_ICONS.length; i++) {
            Identifier texture = BUILTIN_ICONS[i];
            String name;
            if (i == 0) {
                name = "config.pua.icon.default";
            } else {
                name = "config.pua.icon.default" + i;
            }
            availableIcons.add(new IconInfo(texture, name, true));
        }
        
        int index = 4;
        while (index <= 10) {
            Identifier texture = new Identifier(NAMESPACE, ICON_PREFIX + index + ICON_SUFFIX);
            try {
                if (resourceManager.getResource(texture).isPresent()) {
                    availableIcons.add(new IconInfo(texture, "config.pua.icon.custom" + (index - 3), false));
                    LOGGER.info("Found custom marker icon: {}", texture);
                }
            } catch (Exception e) {
            }
            index++;
        }
        
        iconsInitialized = true;
        LOGGER.info("Initialized {} marker icons", availableIcons.size());
    }

    public static int getMaxIconIndex() {
        return availableIcons.isEmpty() ? 0 : availableIcons.size() - 1;
    }

    public static String getIconName(int index) {
        if (index < 0 || index >= availableIcons.size()) {
            index = 0;
        }
        IconInfo info = availableIcons.get(index);
        return net.minecraft.text.Text.translatable(info.name).getString();
    }

    private Identifier getCurrentIconTexture() {
        if (!iconsInitialized) {
            return BUILTIN_ICONS[0];
        }
        
        int index = PunctuationConfig.markerIconIndex;
        if (index < 0 || index >= availableIcons.size()) {
            index = 0;
        }
        
        return availableIcons.get(index).texture;
    }

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

    private void renderMarkerIcon(MatrixStack matrices, VertexConsumerProvider vertexConsumers) {
        MinecraftClient client = MinecraftClient.getInstance();
        Camera camera = client.gameRenderer.getCamera();

        matrices.push();
        matrices.translate(0, 0.7f, 0);
        matrices.multiply(camera.getRotation());
        matrices.multiply(new Quaternionf().rotationY((float) Math.PI));
        matrices.scale(1.5f, 1.5f, 1.5f);

        Identifier currentTexture = getCurrentIconTexture();
        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderLayer.getEntityCutoutNoCull(currentTexture));
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