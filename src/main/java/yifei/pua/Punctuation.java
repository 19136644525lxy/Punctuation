package yifei.pua;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Punctuation implements ModInitializer {
	public static final String MOD_ID = "pua";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Punctuation mod initialized");
		registerNetworking();
	}

	private void registerNetworking() {
		ServerPlayNetworking.registerGlobalReceiver(PunctuationNetworking.TELEPORT_ID,
				(server, player, handler, buf, responseSender) -> {
					BlockPos pos = buf.readBlockPos();
					server.execute(() -> teleportPlayer(player, pos));
				});
	}

	private void teleportPlayer(ServerPlayerEntity player, BlockPos pos) {
		if (player == null || pos == null) return;
		Vec3d targetPos = new Vec3d(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
		player.teleport(targetPos.x, targetPos.y, targetPos.z);
	}

	public static Identifier id(String path) {
		return new Identifier(MOD_ID, path);
	}
}