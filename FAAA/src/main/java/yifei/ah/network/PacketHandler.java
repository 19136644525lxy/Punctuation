package yifei.ah.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;

public class PacketHandler {
    public static void register() {
        ServerPlayNetworking.registerGlobalReceiver(
            MarkerEntityPacket.ID,
            (server, player, handler, buf, responseSender) -> {
                MarkerEntityPacket packet = MarkerEntityPacket.read(buf);
                packet.handle(server, player);
            }
        );
    }
    
    public static void sendMarkerEntityPacket(int entityId) {
        PacketByteBuf buf = PacketByteBufs.create();
        new MarkerEntityPacket(entityId).write(buf);
        ClientPlayNetworking.send(MarkerEntityPacket.ID, buf);
    }
}