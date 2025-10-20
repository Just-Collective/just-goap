package com.just.goap.sensor.retention;

import com.just.goap.StateKey;
import com.just.goap.state.ReadableWorldState;

public interface RetentionPolicy<T, V> {

    StateKey<V> key();

    boolean shouldRecompute(
        T context,
        ReadableWorldState previousWorldState,
        ReadableWorldState currentWorldState
    );
}
