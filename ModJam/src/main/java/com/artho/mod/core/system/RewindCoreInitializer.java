package com.artho.mod.core.system;

import com.artho.mod.core.component.RewindCoreComponent;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.RefSystem;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class RewindCoreInitializer extends RefSystem {

    private static final Query QUERY =
            Query.and(
                    BlockModule.BlockStateInfo.getComponentType(),
                    RewindCoreComponent.getComponentType()
            );

    @Override
    public void onEntityAdded(@NonNullDecl Ref ref, @NonNullDecl AddReason addReason, @NonNullDecl Store store, @NonNullDecl CommandBuffer commandBuffer) {
        BlockModule.BlockStateInfo info =
                (BlockModule.BlockStateInfo) commandBuffer.getComponent(
                        ref, BlockModule.BlockStateInfo.getComponentType());

        if (info == null) return;

        RewindCoreComponent core =
                (RewindCoreComponent) commandBuffer.getComponent(
                        ref, RewindCoreComponent.getComponentType());

        if (core == null) return;

        int x = ChunkUtil.xFromBlockInColumn(info.getIndex());
        int y = ChunkUtil.yFromBlockInColumn(info.getIndex());
        int z = ChunkUtil.zFromBlockInColumn(info.getIndex());

        WorldChunk worldChunk =
                (WorldChunk) commandBuffer.getComponent(
                        info.getChunkRef(), WorldChunk.getComponentType());

        if (worldChunk != null) {
            worldChunk.setTicking(x, y, z, true);
        }
    }

    @Override
    public void onEntityRemove(@NonNullDecl Ref ref, @NonNullDecl RemoveReason removeReason, @NonNullDecl Store store, @NonNullDecl CommandBuffer commandBuffer) {

    }

    @Override
    public Query getQuery() {
        return QUERY;
    }
}
