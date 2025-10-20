package com.just.goap.state;

import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

import com.just.goap.StateKey;
import com.just.goap.effect.EffectContainer;

public final class SimulatedWorldState implements WorldState {

    private final WorldState simulatedWorldState;

    private final ReadableWorldState backingWorldState;

    public SimulatedWorldState(ReadableWorldState backingWorldState) {
        this.simulatedWorldState = WorldState.create();
        this.backingWorldState = backingWorldState;
    }

    @Override
    public <V> @Nullable V getOrNull(StateKey<V> key) {
        // Try simulated overrides first.
        var value = simulatedWorldState.getOrNull(key);

        if (value != null) {
            return value;
        }

        // Otherwise, delegate to sensing world state.
        return backingWorldState.getOrNull(key);
    }

    @Override
    public Map<StateKey<?>, Object> getMap() {
        // Return a merged view (simulated overrides + sensed base).
        var merged = new HashMap<StateKey<?>, Object>();
        merged.putAll(backingWorldState.getMap());
        merged.putAll(simulatedWorldState.getMap());
        return merged;
    }

    @Override
    public <V> void set(StateKey<V> key, V value) {
        simulatedWorldState.set(key, value);
    }

    @Override
    public void setAll(Map<StateKey<?>, Object> map) {
        simulatedWorldState.setAll(map);
    }

    @Override
    public void apply(EffectContainer effectContainer) {
        for (var effect : effectContainer.getEffects()) {
            effect.apply(this);
        }
    }

    @Override
    public void clear() {
        simulatedWorldState.clear();
    }

    public SimulatedWorldState copy() {
        // Create a deep copy of the simulation layer, preserving the same sensing base.
        var copy = new SimulatedWorldState(backingWorldState);
        copy.simulatedWorldState.getMap().putAll(simulatedWorldState.getMap());
        return copy;
    }
}
