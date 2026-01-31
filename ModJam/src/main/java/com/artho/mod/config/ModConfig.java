package com.artho.mod.config;

import com.hypixel.hytale.codec.builder.BuilderCodec;

public class ModConfig {
    public static final BuilderCodec<ModConfig> CODEC;

    static {
        CODEC = BuilderCodec
                .builder(ModConfig.class, ModConfig::new)
                .build();
    }
}
