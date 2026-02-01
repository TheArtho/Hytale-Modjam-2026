package com.artho.mod.core.registry;

import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

public final class RewindEntityRegistry {

    private static final Int2ObjectMap<Entry> BLOCK_TO_ENTITY = new Int2ObjectOpenHashMap<>();
    private static final Object2IntMap<String> ENTITY_TO_BLOCK = new Object2IntOpenHashMap<>();

    public static void register(
            String blockId,
            String entityId
    ) {
        int block = BlockType.getAssetMap().getIndex(blockId);
        BLOCK_TO_ENTITY.put(block, new Entry(block, entityId));
        ENTITY_TO_BLOCK.put(entityId, block);
    }

    public static Entry getByBlock(int blockId) {
        return BLOCK_TO_ENTITY.get(blockId);
    }

    public static Integer getBlockForEntity(String entityId) {
        return ENTITY_TO_BLOCK.getOrDefault(entityId, -1);
    }

    public record Entry(int blockId, String entityId) {}
}
