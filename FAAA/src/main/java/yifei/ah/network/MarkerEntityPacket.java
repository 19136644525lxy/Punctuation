package yifei.ah.network;

import net.minecraft.entity.Entity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class MarkerEntityPacket {
    public static final Identifier ID = new Identifier("ah", "marker_entity");
    
    private final int entityId;
    
    public MarkerEntityPacket(int entityId) {
        this.entityId = entityId;
    }
    
    public static MarkerEntityPacket read(PacketByteBuf buf) {
        return new MarkerEntityPacket(buf.readInt());
    }
    
    public void write(PacketByteBuf buf) {
        buf.writeInt(entityId);
    }
    
    public void handle(MinecraftServer server, ServerPlayerEntity player) {
        server.execute(() -> {
            World world = player.getWorld();
            Entity entity = world.getEntityById(entityId);
            if (entity != null) {
                yifei.ah.manager.FriendlyMobManager.makeFriendly(entity, player);
            }
        });
    }
}