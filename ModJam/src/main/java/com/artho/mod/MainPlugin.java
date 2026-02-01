package com.artho.mod;

import com.artho.mod.core.component.RewindCoreComponent;
import com.artho.mod.core.component.RewindOriginComponent;
import com.artho.mod.core.interaction.RewindInteraction;
import com.artho.mod.core.registry.RewindBlockRegistry;
import com.artho.mod.core.registry.RewindEntityRegistry;
import com.artho.mod.core.registry.RewindRegistry;
import com.artho.mod.core.system.RewindCoreInitializer;
import com.artho.mod.core.system.RewindCoreSystem;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.Config;
import com.artho.mod.config.ModConfig;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class MainPlugin extends JavaPlugin {
    private static HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private Config<ModConfig> config;
    private ComponentType<ChunkStore, RewindCoreComponent> REWIND_CORE_COMPONENT_TYPE;
    private ComponentType<EntityStore, RewindOriginComponent> REWIND_ORIGIN_COMPONENT_TYPE;

    private static MainPlugin INSTANCE;

    public MainPlugin(@NonNullDecl JavaPluginInit init) {
        if (INSTANCE != null) throw new IllegalStateException("Plugin already initialized!");
        super(init);
        INSTANCE = this;
        this.config = this.withConfig("ModConfig", ModConfig.CODEC);
    }

    @Override
    protected void setup() {
        // Setting up the config file
        this.config.save();
        // ChunkStore component registration
        REWIND_CORE_COMPONENT_TYPE = getChunkStoreRegistry().registerComponent(RewindCoreComponent.class, "RewindCore", RewindCoreComponent.CODEC);
        // EntityStore component registration
        REWIND_ORIGIN_COMPONENT_TYPE = getEntityStoreRegistry().registerComponent(RewindOriginComponent.class, "RewindOrigin", RewindOriginComponent.CODEC);
        // Interaction registration
        Interaction.CODEC.register("RewindInteraction", RewindInteraction.class, RewindInteraction.CODEC);
        // System registration
        getChunkStoreRegistry().registerSystem(new RewindCoreInitializer());
        getChunkStoreRegistry().registerSystem(new RewindCoreSystem());
        // Rewind block registry
        RewindRegistry.blockToBlock("Rock_Gold_Brick_Smooth", "Rock_Gold_Brick_Rusty");
        RewindRegistry.blockToBlock("Rock_Gold_Brick_Rusty", "Rock_Gold_Brick_Smooth");
        RewindRegistry.blockToBlock("Soil_Sand", "Soil_Grass");
        RewindRegistry.blockToBlock("Soil_Grass", "Soil_Sand");
        RewindRegistry.blockToBlock("Rock_Stone_Brick", "Rock_Lime_Brick");
        RewindRegistry.blockToBlock("Rock_Stone_Brick_Mossy", "Rock_Lime_Brick_Decorative");
        RewindRegistry.blockToBlock("Rock_Lime_Brick", "Rock_Stone_Brick");
        RewindRegistry.blockToBlock("Rock_Lime_Brick_Decorative", "Rock_Stone_Brick_Mossy");
        RewindRegistry.blockToBlock("Lamp_Off", "Lamp_On");
        RewindRegistry.blockToBlock("Lamp_On", "Lamp_Off");
        RewindRegistry.blockToBlock("Rock_Basalt_Brick_Decorative", "Furniture_Dungeon_Chest_Epic", "FM_Test_Drop_List");
        // Rewind entity registry
        RewindRegistry.blockToEntity("Deco_Bone_Pile", "Skeleton");

        LOGGER.atInfo().log("Mod started!");
    }

    public static MainPlugin get() {
        return INSTANCE;
    }

    public ComponentType<ChunkStore, RewindCoreComponent> getRewindCoreComponentType() {
        return REWIND_CORE_COMPONENT_TYPE;
    }

    public ComponentType<EntityStore, RewindOriginComponent> getRewindOriginComponentType() {
        return REWIND_ORIGIN_COMPONENT_TYPE;
    }
}
