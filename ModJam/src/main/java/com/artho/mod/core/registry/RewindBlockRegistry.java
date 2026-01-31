package com.artho.mod.core.registry;

import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;

import java.util.HashMap;
import java.util.Map;

public final class RewindBlockRegistry {

    private static final Int2IntMap MAP = new Int2IntOpenHashMap();
    private static final Map<String, String> PENDING = new HashMap<>();
    private static boolean resolved = false;

    public static void registerRewind(String from, String to) {
        PENDING.put(from, to);
    }

    private static void resolveIfNeeded() {
        if (resolved) return;

        for (var e : PENDING.entrySet()) {
            int fromId = BlockType.getAssetMap().getIndex(e.getKey());
            int toId   = BlockType.getAssetMap().getIndex(e.getValue());

            if (fromId == Integer.MIN_VALUE || toId == Integer.MIN_VALUE) {
                return;
            }

            MAP.put(fromId, toId);
        }

        resolved = true;
        PENDING.clear();
    }

    public static Integer getRewound(int blockId) {
        resolveIfNeeded();
        return MAP.containsKey(blockId) ? MAP.get(blockId) : null;
    }
}
