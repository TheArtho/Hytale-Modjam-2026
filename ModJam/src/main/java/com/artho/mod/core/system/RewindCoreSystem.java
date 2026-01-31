package com.artho.mod.core.system;

import com.artho.mod.core.component.RewindCoreComponent;
import com.artho.mod.core.registry.RewindBlockRegistry;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.server.core.asset.type.blocktick.BlockTickStrategy;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockComponentChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection;
import com.hypixel.hytale.server.core.universe.world.chunk.section.ChunkSection;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

public class RewindCoreSystem extends EntityTickingSystem {

    public static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    private static final Query QUERY =
            Query.and(BlockSection.getComponentType(), ChunkSection.getComponentType());

    @Override
    public void tick(float v, int index, @NonNullDecl ArchetypeChunk chunk, @NonNullDecl Store store, @NonNullDecl CommandBuffer commandBuffer) {
        BlockSection blocks =
                (BlockSection) chunk.getComponent(index, BlockSection.getComponentType());

        if (blocks.getTickingBlocksCountCopy() == 0) return;

        ChunkSection section =
                (ChunkSection) chunk.getComponent(index, ChunkSection.getComponentType());

        BlockComponentChunk blockComponentChunk =
                (BlockComponentChunk) commandBuffer.getComponent(
                        section.getChunkColumnReference(),
                        BlockComponentChunk.getComponentType());

        blocks.forEachTicking(
                blockComponentChunk,
                commandBuffer,
                section.getY(),
                (bcc, cb, lx, ly, lz, blockId) -> {
                    Ref<ChunkStore> blockRef =
                            bcc.getEntityReference(
                                    ChunkUtil.indexBlockInColumn(lx, ly, lz));

                    if (blockRef == null) {
                        return BlockTickStrategy.IGNORED;
                    }

                    RewindCoreComponent core =
                            (RewindCoreComponent) cb.getComponent(
                                    blockRef, RewindCoreComponent.getComponentType());

                    if (core == null || !core.active) {
                        return BlockTickStrategy.CONTINUE;
                    }

                    core.tickCounter++;
                    if (core.tickCounter < core.ticksPerStep) {
                        return BlockTickStrategy.CONTINUE;
                    }

                    core.tickCounter = 0;
                    core.currentRadius++;

                    WorldChunk wc =
                            (WorldChunk) cb.getComponent(
                                    section.getChunkColumnReference(),
                                    WorldChunk.getComponentType());

                    int gx = lx + wc.getX() * 32;
                    int gz = lz + wc.getZ() * 32;

                    applyRewinding(wc.getWorld(), gx, ly, gz, core);

                    if (core.currentRadius >= core.maxRadius) {
                        core.active = false;
                        core.currentRadius = 0;
                        LOGGER.atInfo().log("End of the rewind.");
                    }

                    return BlockTickStrategy.CONTINUE;
                }
        );
    }

    @NullableDecl
    @Override
    public Query getQuery() {
        return QUERY;
    }

    private void applyRewinding(
            World world,
            int cx,
            int cy,
            int cz,
            RewindCoreComponent core
    ) {
        int r = core.currentRadius;
        int r2 = r * r;
        int prevR2 = (r - 1) * (r - 1);

        for (int dx = -r; dx <= r; dx++) {
            for (int dy = -r; dy <= r; dy++) {
                for (int dz = -r; dz <= r; dz++) {

                    int d2 = dx * dx + dy * dy + dz * dz;

                    // Only process the current ring (shell)
                    if (d2 <= r2 && d2 > prevR2) {

                        int x = cx + dx;
                        int y = cy + dy;
                        int z = cz + dz;

                        // Skip invalid Y
                        if (y < 0 || y >= 320) {
                            LOGGER.atInfo().log("Invalid Y, skipping...");
                            continue;
                        }

                        long chunkIndex = ChunkUtil.indexChunkFromBlock(x, z);
                        WorldChunk chunk = world.getChunkIfInMemory(chunkIndex);

                        // Skip unloaded chunks
                        if (chunk == null) {
                            LOGGER.atInfo().log("Chunk is null, skipping...");
                            continue;
                        }

                        int localX = x & 31;
                        int localZ = z & 31;

                        int blockId = chunk.getBlock(localX, y, localZ);
                        Integer newBlockId = RewindBlockRegistry.getRewound(blockId);

                        if (newBlockId == null) {
                            LOGGER.atInfo().log("new block Id is null, skipping...");
                            continue;
                        }

                        LOGGER.atInfo().log("Try Setting block at %d, %d, %d to %d...".formatted(x, y, z, newBlockId));

                        // IMPORTANT: world.execute ensures thread safety
                        world.execute(() -> {
                            LOGGER.atInfo().log("Block set at %d, %d, %d to %d.".formatted(x, y, z, newBlockId));
                            chunk.setBlock(localX, y, localZ, newBlockId);
                        });
                    }
                }
            }
        }
    }

}
