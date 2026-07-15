package yifei.pua.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;
import yifei.pua.PunctuationNetworking;
import yifei.pua.api.PunctuationAPIAccess;
import yifei.pua.api.RaycastAPI;
import yifei.pua.config.PunctuationConfig;

import java.util.UUID;

public class PunctuationClient implements ClientModInitializer {
    public static BlockPos markerPos = null;
    public static UUID markerEntityId = null;
    public static ItemStack markerItemStack = ItemStack.EMPTY;
    public static Vec3d markerEntityPos = null;
    public static MarkerType markerType = MarkerType.NONE;

    public static Mode currentMode = Mode.MARKER;

    private boolean wasMiddleButtonPressed = false;

    private final KeyBinding teleportKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.pua.teleport",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_P,
            "category.pua"
    ));

    private final KeyBinding toggleModeKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.pua.toggle_mode",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_LEFT_ALT,
            "category.pua"
    ));

    @Override
    public void onInitializeClient() {
        PunctuationConfig.init(MinecraftClient.getInstance().runDirectory.toPath().resolve("config"));
        registerInputHandling();
        registerRendering();
        registerTeleportKey();
        registerToggleModeKey();
    }

    private void registerInputHandling() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.currentScreen != null) return;

            if (currentMode == Mode.MARKER) {
                long window = client.getWindow().getHandle();
                boolean isMiddleButtonPressed = GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_MIDDLE) == GLFW.GLFW_PRESS;

                if (isMiddleButtonPressed && !wasMiddleButtonPressed) {
                    wasMiddleButtonPressed = true;
                    handleMiddleClick(client);
                } else if (!isMiddleButtonPressed) {
                    wasMiddleButtonPressed = false;
                }
            }

            if (markerType != MarkerType.NONE) {
                spawnContinuousParticles();
            }
        });
    }

    private void spawnContinuousParticles() {
        if (markerType == MarkerType.ENTITY && markerEntityPos != null) {
            PunctuationAPIAccess.getParticleAPI().spawnContinuousEntityParticles(markerEntityPos);
        }
    }

    private void handleMiddleClick(MinecraftClient client) {
        PlayerEntity player = client.player;
        if (player == null || client.world == null) return;

        int renderDistance = client.options.getViewDistance().getValue() * 16;

        RaycastAPI raycastAPI = PunctuationAPIAccess.getRaycastAPI();
        HitResult hitResult = raycastAPI.raycast(player, renderDistance);

        if (hitResult == null) {
            player.sendMessage(Text.translatable("message.pua.no_target"), true);
            return;
        }

        if (hitResult.getType() == HitResult.Type.BLOCK) {
            BlockHitResult blockHit = (BlockHitResult) hitResult;
            Vec3d hitPos = blockHit.getPos();
            double distance = hitPos.distanceTo(player.getPos());

            if (distance <= renderDistance) {
                markerPos = blockHit.getBlockPos().offset(blockHit.getSide());
                markerEntityId = null;
                markerItemStack = ItemStack.EMPTY;
                markerEntityPos = null;
                markerType = MarkerType.BLOCK;

                PunctuationAPIAccess.getParticleAPI().spawnMarkerParticles(new Vec3d(markerPos.getX() + 0.5, markerPos.getY() + 0.5, markerPos.getZ() + 0.5));
                player.sendMessage(Text.translatable("message.pua.marker_set", markerPos.getX(), markerPos.getY(), markerPos.getZ()), true);
            } else {
                player.sendMessage(Text.translatable("message.pua.out_of_range"), true);
            }
        }
    }

    private void registerRendering() {
        WorldRenderEvents.LAST.register(context -> {
            if (markerType == MarkerType.NONE) return;

            MinecraftClient client = MinecraftClient.getInstance();
            PlayerEntity player = client.player;
            if (player == null || client.world == null) return;

            MatrixStack matrices = context.matrixStack();

            if (markerType == MarkerType.BLOCK && markerPos != null) {
                PunctuationAPIAccess.getRenderAPI().renderBlockMarker(matrices, markerPos);
            }
        });
    }

    private ItemEntity findItemEntity(MinecraftClient client, UUID uuid) {
        if (client.world == null || client.player == null) return null;

        int renderDistance = client.options.getViewDistance().getValue() * 16;
        Vec3d playerPos = client.player.getPos();
        Box searchBox = new Box(
                playerPos.add(-renderDistance, -renderDistance, -renderDistance),
                playerPos.add(renderDistance, renderDistance, renderDistance)
        );

        return client.world.getEntitiesByClass(ItemEntity.class, searchBox, entity -> entity.getUuid().equals(uuid)).stream()
                .findFirst()
                .orElse(null);
    }

    private void registerTeleportKey() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.currentScreen != null) return;

            while (teleportKey.wasPressed()) {
                if (markerType == MarkerType.BLOCK && markerPos != null) {
                    ClientPlayNetworking.send(PunctuationNetworking.TELEPORT_ID,
                            PunctuationNetworking.createTeleportPacket(markerPos));
                    client.player.sendMessage(Text.translatable("message.pua.teleporting"), true);
                    clearMarker();
                } else if (markerType == MarkerType.ENTITY) {
                    client.player.sendMessage(Text.translatable("message.pua.cannot_teleport_item"), true);
                } else {
                    client.player.sendMessage(Text.translatable("message.pua.no_marker"), true);
                }
            }
        });
    }

    private void registerToggleModeKey() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.currentScreen != null) return;

            while (toggleModeKey.wasPressed()) {
                currentMode = currentMode == Mode.MARKER ? Mode.PICK_BLOCK : Mode.MARKER;
                clearMarker();
                String modeKey = currentMode == Mode.MARKER ? "message.pua.mode_marker" : "message.pua.mode_pick_block";
                client.player.sendMessage(Text.translatable("message.pua.mode_switched", Text.translatable(modeKey)), true);
            }
        });
    }

    public static void clearMarker() {
        markerPos = null;
        markerEntityId = null;
        markerItemStack = ItemStack.EMPTY;
        markerEntityPos = null;
        markerType = MarkerType.NONE;
    }

    public enum Mode {
        MARKER,
        PICK_BLOCK
    }

    public enum MarkerType {
        NONE,
        BLOCK,
        ENTITY
    }
}