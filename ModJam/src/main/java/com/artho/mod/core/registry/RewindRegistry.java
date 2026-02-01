package com.artho.mod.core.registry;

import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public final class RewindRegistry {

    private static final Int2ObjectMap<RewindTransform> MAP =
            new Int2ObjectOpenHashMap<>();

    private static final Map<String, RewindTransform.Pending> PENDING =
            new HashMap<>();

    private static boolean resolved = false;

    private RewindRegistry() {}

    public static void blockToBlock(String from, String to) {
        blockToBlock(from, to, null);
    }

    public static void blockToBlock(
            String from,
            String to,
            @Nullable String dropListId
    ) {
        PENDING.put(from, new RewindTransform.Pending.BlockToBlock(to, dropListId));
    }

    public static void blockToEntity(
            String from,
            String entityId
    ) {
        PENDING.put(from, new RewindTransform.Pending.BlockToEntity(entityId));
    }

    private static void resolveIfNeeded() {
        if (resolved) return;

        for (var entry : PENDING.entrySet()) {
            String fromName = entry.getKey();
            RewindTransform.Pending pending = entry.getValue();

            int fromId = BlockType.getAssetMap().getIndex(fromName);
            if (fromId == Integer.MIN_VALUE) {
                return;
            }

            if (pending instanceof RewindTransform.Pending.BlockToBlock btb) {

                int toId = BlockType.getAssetMap().getIndex(btb.to());
                if (toId == Integer.MIN_VALUE) {
                    return;
                }

                MAP.put(
                        fromId,
                        new RewindTransform.BlockToBlock(
                                fromId,
                                toId,
                                btb.dropListId()
                        )
                );
            }

            else if (pending instanceof RewindTransform.Pending.BlockToEntity bte) {

                MAP.put(
                        fromId,
                        new RewindTransform.BlockToEntity(
                                fromId,
                                bte.entityId()
                        )
                );
            }
        }

        resolved = true;
        PENDING.clear();
    }

    @Nullable
    public static RewindTransform get(int blockId) {
        resolveIfNeeded();
        return MAP.get(blockId);
    }
}
