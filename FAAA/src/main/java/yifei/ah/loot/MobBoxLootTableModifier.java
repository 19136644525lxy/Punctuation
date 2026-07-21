package yifei.ah.loot;

import net.fabricmc.fabric.api.loot.v2.LootTableEvents;
import net.fabricmc.fabric.api.server.PlayerStream;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.condition.RandomChanceLootCondition;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.function.SetCountLootFunction;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.WorldChunk;
import yifei.ah.item.ModItems;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MobBoxLootTableModifier {
    private static final Map<Identifier, Float> LOOT_TABLE_CHANCES = new HashMap<>();
    private static final ConcurrentHashMap<String, Boolean> GENERATED_CHUNKS = new ConcurrentHashMap<>();

    static {
        LOOT_TABLE_CHANCES.put(new Identifier("minecraft", "chests/simple_dungeon"), 0.15f);
        LOOT_TABLE_CHANCES.put(new Identifier("minecraft", "chests/abandoned_mineshaft"), 0.15f);
        LOOT_TABLE_CHANCES.put(new Identifier("minecraft", "chests/stronghold_corridor"), 0.15f);
        LOOT_TABLE_CHANCES.put(new Identifier("minecraft", "chests/stronghold_crossing"), 0.15f);
        LOOT_TABLE_CHANCES.put(new Identifier("minecraft", "chests/stronghold_library"), 0.15f);
        LOOT_TABLE_CHANCES.put(new Identifier("minecraft", "chests/woodland_mansion"), 0.15f);
        LOOT_TABLE_CHANCES.put(new Identifier("minecraft", "chests/buried_treasure"), 0.15f);
        LOOT_TABLE_CHANCES.put(new Identifier("minecraft", "chests/desert_pyramid"), 0.15f);
        LOOT_TABLE_CHANCES.put(new Identifier("minecraft", "chests/jungle_temple"), 0.15f);
        LOOT_TABLE_CHANCES.put(new Identifier("minecraft", "chests/ocean_ruin_cold"), 0.15f);
        LOOT_TABLE_CHANCES.put(new Identifier("minecraft", "chests/ocean_ruin_warm"), 0.15f);
        LOOT_TABLE_CHANCES.put(new Identifier("minecraft", "chests/shipwreck_treasure"), 0.15f);
        LOOT_TABLE_CHANCES.put(new Identifier("minecraft", "chests/shipwreck_supply"), 0.15f);
        LOOT_TABLE_CHANCES.put(new Identifier("minecraft", "chests/shipwreck_map"), 0.15f);
        LOOT_TABLE_CHANCES.put(new Identifier("minecraft", "chests/trail_ruins"), 0.15f);
        LOOT_TABLE_CHANCES.put(new Identifier("minecraft", "chests/ancient_city"), 0.15f);

        LOOT_TABLE_CHANCES.put(new Identifier("minecraft", "chests/nether_bridge"), 0.25f);
        LOOT_TABLE_CHANCES.put(new Identifier("minecraft", "chests/bastion_bridge"), 0.25f);
        LOOT_TABLE_CHANCES.put(new Identifier("minecraft", "chests/bastion_treasure"), 0.25f);
        LOOT_TABLE_CHANCES.put(new Identifier("minecraft", "chests/bastion_other"), 0.25f);
        LOOT_TABLE_CHANCES.put(new Identifier("minecraft", "chests/bastion_hoglin_stable"), 0.25f);

        LOOT_TABLE_CHANCES.put(new Identifier("minecraft", "chests/end_city_treasure"), 0.75f);
        LOOT_TABLE_CHANCES.put(new Identifier("minecraft", "chests/end_city_ship"), 0.75f);
    }

    private static String getChunkKey(ServerWorld world, BlockPos pos) {
        ChunkPos chunkPos = new ChunkPos(pos);
        return world.getRegistryKey().getValue().toString() + ":" + chunkPos.x + ":" + chunkPos.z;
    }

    private static boolean tryGenerate(ServerWorld world, BlockPos pos) {
        String key = getChunkKey(world, pos);
        if (GENERATED_CHUNKS.containsKey(key)) {
            return false;
        }
        GENERATED_CHUNKS.put(key, true);
        return true;
    }

    public static void register() {
        LootTableEvents.MODIFY.register((resourceManager, lootManager, id, tableBuilder, source) -> {
            Float chance = LOOT_TABLE_CHANCES.get(id);
            if (chance != null) {
                LootPool.Builder poolBuilder = LootPool.builder()
                        .rolls(ConstantLootNumberProvider.create(1))
                        .with(ItemEntry.builder(ModItems.MOB_BOX)
                                .apply(SetCountLootFunction.builder(ConstantLootNumberProvider.create(1))))
                        .conditionally(RandomChanceLootCondition.builder(chance));

                tableBuilder.pool(poolBuilder.build());
            }
        });

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            GENERATED_CHUNKS.clear();
        });
    }
}