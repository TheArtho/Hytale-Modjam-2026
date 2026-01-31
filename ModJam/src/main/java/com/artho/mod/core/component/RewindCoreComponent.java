package com.artho.mod.core.component;

import com.artho.mod.MainPlugin;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;

public class RewindCoreComponent implements Component<ChunkStore> {

    public static final BuilderCodec<RewindCoreComponent> CODEC;

    public int maxRadius = 8;
    public int currentRadius = 0;
    public int ticksPerStep = 4;
    public int tickCounter = 0;

    public boolean active = false;
    public boolean finished = false;

    public RewindCoreComponent() {}

    public static ComponentType getComponentType() {
        return MainPlugin.get().getRewindCoreComponentType();
    }

    @Override
    public Component<ChunkStore> clone() {
        RewindCoreComponent copy = new RewindCoreComponent();
        copy.maxRadius = this.maxRadius;
        return copy;
    }

    static {
        CODEC = BuilderCodec.builder(RewindCoreComponent.class, RewindCoreComponent::new)
                .append(new KeyedCodec<>("MaxRadius", Codec.INTEGER),
                        (c, v) -> c.maxRadius = v,
                        c -> c.maxRadius)
                .add()
                .build();
    }
}
