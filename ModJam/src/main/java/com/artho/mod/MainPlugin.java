package com.artho.mod;

import com.artho.mod.core.component.RewindCoreComponent;
import com.artho.mod.core.interaction.RewindInteraction;
import com.artho.mod.core.registry.RewindBlockRegistry;
import com.artho.mod.core.system.RewindCoreInitializer;
import com.artho.mod.core.system.RewindCoreSystem;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.util.Config;
import com.artho.mod.config.ModConfig;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class MainPlugin extends JavaPlugin {
    private static HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private Config<ModConfig> config;
    private ComponentType<ChunkStore, RewindCoreComponent> REWIND_CORE_COMPONENT_TYPE;

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
        // Nothing for now
        // Interaction registration
        Interaction.CODEC.register("RewindInteraction", RewindInteraction.class, RewindInteraction.CODEC);
        // System registration
        getChunkStoreRegistry().registerSystem(new RewindCoreInitializer());
        getChunkStoreRegistry().registerSystem(new RewindCoreSystem());
        // Rewind block registry
        RewindBlockRegistry.registerRewind("Trigger_Crystal_On", "Trigger_Crystal_Off");
        RewindBlockRegistry.registerRewind("Trigger_Crystal_Off", "Trigger_Crystal_On");
        RewindBlockRegistry.registerRewind("Soil_Sand", "Soil_Grass");
        RewindBlockRegistry.registerRewind("Soil_Sand", "Soil_Grass");
        RewindBlockRegistry.registerRewind("Soil_Grass", "Soil_Sand");
        RewindBlockRegistry.registerRewind("Rock_Stone_Brick", "Rock_Lime_Brick");
        RewindBlockRegistry.registerRewind("Rock_Stone_Brick_Mossy", "Rock_Lime_Brick_Decorative");
        RewindBlockRegistry.registerRewind("Rock_Lime_Brick", "Rock_Stone_Brick");
        RewindBlockRegistry.registerRewind("Rock_Lime_Brick_Decorative", "Rock_Stone_Brick_Mossy");
        RewindBlockRegistry.registerRewind("Lamp_Off", "Lamp_On");
        RewindBlockRegistry.registerRewind("Lamp_On", "Lamp_Off");

        LOGGER.atInfo().log("Mod started!");
    }

    public static MainPlugin get() {
        return INSTANCE;
    }

    public ComponentType<ChunkStore, RewindCoreComponent> getRewindCoreComponentType() {
        return REWIND_CORE_COMPONENT_TYPE;
    }
}
