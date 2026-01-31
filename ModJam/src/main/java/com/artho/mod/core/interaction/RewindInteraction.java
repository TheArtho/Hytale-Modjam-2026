package com.artho.mod.core.interaction;

import com.artho.mod.core.component.RewindCoreComponent;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.protocol.BlockPosition;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInteraction;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import javax.annotation.Nonnull;

public class RewindInteraction extends SimpleInteraction {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    @Nonnull
    public static final BuilderCodec<RewindInteraction> CODEC;
    public Integer radius = 5;

    @Override
    protected void tick0(boolean firstRun, float time, @NonNullDecl InteractionType type, @NonNullDecl InteractionContext context, @NonNullDecl CooldownHandler cooldownHandler) {
        LOGGER.atInfo().log("Rewinding!");

        CommandBuffer cb = context.getCommandBuffer();

        if (cb == null) {
            LOGGER.atInfo().log("cb is null!");
            return;
        }

        BlockPosition targetBlock = context.getTargetBlock();
        if (targetBlock == null) {
            LOGGER.atInfo().log("targetBlock is null!");
            return;
        }
        EntityStore entityStore = (EntityStore)cb.getExternalData();
        World world = (entityStore).getWorld();
        ChunkStore chunkStore = world.getChunkStore();
        Store<ChunkStore> chunkEcsStore = chunkStore.getStore();
        WorldChunk chunk = world.getChunkIfInMemory(
                ChunkUtil.indexChunkFromBlock(targetBlock.x, targetBlock.z)
        );

        if (chunk == null) {
            LOGGER.atInfo().log("chunk is null!");
            return;
        }

        Ref<ChunkStore> blockRef = chunk.getBlockComponentEntity(targetBlock.x, targetBlock.y, targetBlock.z);

        if (blockRef == null) {
            LOGGER.atInfo().log("blockRef is null!");
            return;
        }

        RewindCoreComponent rewindCoreComponent = (RewindCoreComponent)chunkEcsStore.getComponent(blockRef, RewindCoreComponent.getComponentType());

        Ref<EntityStore> ownerRef = context.getOwningEntity();
        Player player = (Player)cb.getComponent(ownerRef, Player.getComponentType());
        if (rewindCoreComponent == null) {
            if (player != null) {
                player.sendMessage(Message.raw("This block is not a rewind block!"));
            }
            LOGGER.atInfo().log("This block is not a rewind block!");
            return;
        }

        LOGGER.atInfo().log("Success!");
        rewindCoreComponent.active = true;
        rewindCoreComponent.maxRadius = radius;

        super.tick0(firstRun, time, type, context, cooldownHandler);
    }

    @Override
    protected void simulateTick0(boolean firstRun, float time, @NonNullDecl InteractionType type, @NonNullDecl InteractionContext context, @NonNullDecl CooldownHandler cooldownHandler) {
        super.simulateTick0(firstRun, time, type, context, cooldownHandler);
    }

    static {
        CODEC = BuilderCodec.builder(RewindInteraction.class, RewindInteraction::new)
                .documentation(
                        "Applies a rewind effect in the specified radius."
                )
                .appendInherited(
                new KeyedCodec<>("Radius", Codec.INTEGER),
                        (i, v) -> i.radius = v,
                        i -> i.radius,
                        (i, parent) -> i.radius = parent.radius
                                )
                                .add()
                .build();
    }
}
