package com.artho.mod.core.utils;

import com.artho.mod.core.component.RewindOriginComponent;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.asset.type.item.config.ItemDropList;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.modules.item.ItemModule;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.meta.BlockState;
import com.hypixel.hytale.server.core.universe.world.meta.state.ItemContainerState;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.npc.asset.builder.BuilderInfo;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import it.unimi.dsi.fastutil.Pair;

import java.util.*;

public final class RewindUtils {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    public static Ref<EntityStore> spawnRewindEntity(
            World world,
            Store<EntityStore> entityStore,
            String roleId,
            int x,
            int y,
            int z,
            int originalBlockId
    ) {
        NPCPlugin npcPlugin = NPCPlugin.get();

        int roleIndex = npcPlugin.getIndex(roleId);
        BuilderInfo roleInfo = npcPlugin.getRoleBuilderInfo(roleIndex);

        if (roleInfo == null) {
            LOGGER.atInfo().log("Role not found: " + roleId);
            return null;
        }

        Vector3d position = new Vector3d(
                x + 0.5,
                y,
                z + 0.5
        );

        Vector3f rotation = new Vector3f(0, 0, 0);

        Pair<Ref<EntityStore>, NPCEntity> npcPair = npcPlugin.spawnEntity(
                entityStore,
                roleIndex,
                position,
                rotation,
                null,
                null
        );

        if (npcPair == null) return null;

        Ref<EntityStore> ref = npcPair.first();

        RewindOriginComponent origin = new RewindOriginComponent();
        origin.blockId = originalBlockId;
        entityStore.addComponent(ref, RewindOriginComponent.getComponentType(), origin);

        return ref;
    }

    public static void generateLootIfContainer(
            World world,
            int x, int y, int z,
            String dropListId
    ) {
        BlockState state = world.getState(x, y, z, true);

        if (!(state instanceof ItemContainerState containerState)) {
            return;
        }

        ItemDropList dropList =
                ItemDropList.getAssetMap().getAsset(dropListId);

        if (dropList == null || dropList.getContainer() == null) {
            return;
        }

        List<ItemStack> drops =
                ItemModule.get().getRandomItemDrops(dropListId);

        if (drops.isEmpty()) {
            return;
        }

        ItemContainer container = containerState.getItemContainer();

        if (!container.clear().succeeded()) {
            return;
        }

        for (ItemStack stack : drops) {
            if (stack == null) continue;

            if (!container.addItemStack(stack).succeeded()) {
                break;
            }
        }
    }
}