package com.artho.mod.core.component;

import com.artho.mod.MainPlugin;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

public class RewindOriginComponent implements Component<EntityStore> {

    public static final BuilderCodec<RewindOriginComponent> CODEC;
    public int blockId;

    public static ComponentType getComponentType() {
        return MainPlugin.get().getRewindOriginComponentType();
    }

    @NullableDecl
    @Override
    public Component<EntityStore> clone() {
        RewindOriginComponent copy = new RewindOriginComponent();
        copy.blockId = this.blockId;
        return copy;
    }

    static {
        CODEC = BuilderCodec.builder(RewindOriginComponent.class, RewindOriginComponent::new)
                .append(new KeyedCodec<>("BlockId", Codec.INTEGER),
                        (c, v) -> c.blockId = v,
                        c -> c.blockId)
                .add()
                .build();
    }
}