package com.artho.mod.core.registry;

import javax.annotation.Nullable;

public sealed interface RewindTransform {

    int from();

    record BlockToBlock(
            int from,
            int to,
            @Nullable String dropListId
    ) implements RewindTransform {
        public boolean hasDropList() {
            return dropListId != null;
        }
    }

    record BlockToEntity(
            int from,
            String entityId
    ) implements RewindTransform {}

    sealed interface Pending {

        record BlockToBlock(
                String to,
                @Nullable String dropListId
        ) implements Pending {}

        record BlockToEntity(
                String entityId
        ) implements Pending {}
    }
}
