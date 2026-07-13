package yifei.pua;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public class PunctuationNetworking {
    public static final Identifier TELEPORT_ID = Punctuation.id("teleport");

    public static PacketByteBuf createTeleportPacket(BlockPos pos) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeBlockPos(pos);
        return buf;
    }
}