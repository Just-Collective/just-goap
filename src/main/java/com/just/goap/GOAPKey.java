package com.just.goap;

import org.jetbrains.annotations.NotNull;

public record GOAPKey<T>(String id) {

    @Override
    public @NotNull String toString() {
        return id;
    }
}
