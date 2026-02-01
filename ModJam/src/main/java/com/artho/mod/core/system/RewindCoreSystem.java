package com.artho.mod.core.system;

import com.artho.mod.core.component.RewindCoreComponent;
import com.artho.mod.core.registry.RewindRegistry;
import com.artho.mod.core.registry.RewindTransform;
import com.artho.mod.core.utils.RewindUtils;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.protocol.DrawType;
import com.hypixel.hytale.server.core.asset.type.blocktick.BlockTickStrategy;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.universe.world.ParticleUtil;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockComponentChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection;
import com.hypixel.hytale.server.core.universe.world.chunk.section.ChunkSection;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

import java.util.ArrayList;
import java.util.List;

public class RewindCoreSystem extends EntityTickingSystem {

    private static final String BLOCK_PARTICLES = "RewindBlock_Particles";
    private static final int MAX_PARTICLE_RADIUS = 5;

    public static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    private static final Query QUERY =
            Query.and(BlockSection.getComponentType(), ChunkSection.getComponentType());

    @Override
    public void tick(
            float v,
            int index,
            @NonNullDecl ArchetypeChunk chunk,
            @NonNullDecl Store store,
            @NonNullDecl CommandBuffer commandBuffer
    ) {
        BlockSection blocks =
                (BlockSection) chunk.getComponent(index, BlockSection.getComponentType());

        if (blocks.getTickingBlocksCountCopy() == 0) return;

        ChunkSection section =
                (ChunkSection) chunk.getComponent(index, ChunkSection.getComponentType());

        BlockComponentChunk blockComponentChunk =
                (BlockComponentChunk) commandBuffer.getComponent(
                        section.getChunkColumnReference(),
                        BlockComponentChunk.getComponentType()
                );

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
                                    blockRef,
                                    RewindCoreComponent.getComponentType()
                            );

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
                                    WorldChunk.getComponentType()
                            );

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

        List<Runnable> actions = new ArrayList<>();

        boolean spawnParticles = core.currentRadius <= MAX_PARTICLE_RADIUS;

        for (int dx = -r; dx <= r; dx++) {
            for (int dy = -r; dy <= r; dy++) {
                for (int dz = -r; dz <= r; dz++) {

                    int d2 = dx * dx + dy * dy + dz * dz;
                    if (d2 <= r2 && d2 > prevR2) {

                        int x = cx + dx;
                        int y = cy + dy;
                        int z = cz + dz;

                        if (y < 0 || y >= 320) continue;

                        WorldChunk chunk =
                                world.getChunkIfInMemory(
                                        ChunkUtil.indexChunkFromBlock(x, z));

                        if (chunk == null) continue;

                        int lx = x & 31;
                        int lz = z & 31;

                        int blockId = chunk.getBlock(lx, y, lz);
                        RewindTransform transform =
                                RewindRegistry.get(blockId);

                        if (transform == null) continue;

                        // Capture immutable values
                        final int fx = x;
                        final int fy = y;
                        final int fz = z;
                        final int flx = lx;
                        final int flz = lz;
                        final WorldChunk fChunk = chunk;
                        final RewindTransform fTransform = transform;

                        actions.add(() -> {

                            // Block to Block
                            if (fTransform instanceof RewindTransform.BlockToBlock btb) {
                                fChunk.setBlock(flx, fy, flz, btb.to());

                                if (spawnParticles && isBlockExposed(world, fx, fy, fz)) {
                                    ParticleUtil.spawnParticleEffect(
                                            BLOCK_PARTICLES,
                                            new Vector3d(fx + 0.5, fy + 0.5, fz + 0.5),
                                            world.getEntityStore().getStore()
                                    );
                                }

                                // Look for a Droplist
                                if (btb.hasDropList()) {
                                    RewindUtils.generateLootIfContainer(
                                            world,
                                            fx, fy, fz,
                                            btb.dropListId()
                                    );
                                }
                            }
                            // Block to Entity
                            else if (fTransform instanceof RewindTransform.BlockToEntity bte) {
                                fChunk.setBlock(flx, fy, flz, 0);
                                RewindUtils.spawnRewindEntity(
                                        world,
                                        world.getEntityStore().getStore(),
                                        bte.entityId(),
                                        fx, fy, fz,
                                        blockId
                                );
                            }
                        });
                    }
                }
            }
        }

        if (!actions.isEmpty()) {
            world.execute(() -> actions.forEach(Runnable::run));
        }
    }

    private static final int[][] NEIGHBORS = {
            { 1, 0, 0 },
            { -1, 0, 0 },
            { 0, 1, 0 },
            { 0, -1, 0 },
            { 0, 0, 1 },
            { 0, 0, -1 }
    };

    private boolean isBlockExposed(World world, int x, int y, int z) {
        for (int[] n : NEIGHBORS) {
            int nx = x + n[0];
            int ny = y + n[1];
            int nz = z + n[2];

            if (ny < 0 || ny >= 320) {
                return true; // Outside world = exposed
            }

            WorldChunk neighborChunk =
                    world.getChunkIfInMemory(
                            ChunkUtil.indexChunkFromBlock(nx, nz));

            if (neighborChunk == null) {
                return true; // Unloaded chunk = assume visible
            }

            int lx = nx & 31;
            int lz = nz & 31;

            int neighborBlockId = neighborChunk.getBlock(lx, ny, lz);
            BlockType neighborType = neighborChunk.getBlockType(lx, ny, lz);

            if (neighborType == null) {
                return true;
            }

            // Core condition: not a full solid block
            if (neighborType.getDrawType() == DrawType.Empty || neighborType.getDrawType() == DrawType.Model) {
                return true;
            }
        }

        return false;
    }

}
