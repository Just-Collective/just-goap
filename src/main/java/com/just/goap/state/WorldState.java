package com.just.goap.state;

import com.just.goap.StateKey;
import com.just.goap.effect.EffectContainer;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public interface WorldState extends ReadableWorldState, WritableWorldState {

    static WorldState create() {
        return create(new HashMap<>());
    }

    static WorldState create(Map<StateKey<?>, Object> stateMap) {
        return new WorldState() {

            @Override
            @SuppressWarnings("unchecked")
            public <T> @Nullable T getOrNull(StateKey<T> key) {
                return (T) stateMap.get(key);
            }

            @Override
            public Map<StateKey<?>, ?> getMap() {
                return stateMap;
            }

            @Override
            public <T> void set(StateKey<T> key, T value) {
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
