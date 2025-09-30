package com.just.goap.state;

import com.just.goap.GOAPKey;
import com.just.goap.effect.EffectContainer;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public interface WorldState extends ReadableWorldState, WritableWorldState {

    static WorldState create() {
        return create(new HashMap<>());
    }

    static WorldState create(Map<GOAPKey<?>, Object> stateMap) {
        return new WorldState() {

            @Override
            @SuppressWarnings("unchecked")
            public <T> @Nullable T getOrNull(GOAPKey<T> key) {
                return (T) stateMap.get(key);
            }

            @Override
            public Map<GOAPKey<?>, ?> getMap() {
                return stateMap;
            }

            @Override
            public <T> void set(GOAPKey<T> key, T value) {
                stateMap.put(key, value);
            }

            @Override
            public void apply(EffectContainer effectContainer) {
                effectContainer.getEffects().forEach(effect -> effect.apply(this));
            }

            @Override
            public WritableWorldState copy() {
                return WorldState.create(new HashMap<>(stateMap));
            }
        };
    }
}
