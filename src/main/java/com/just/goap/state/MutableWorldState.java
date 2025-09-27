package com.just.goap.state;

import com.just.goap.TypedIdentifier;
import com.just.goap.effect.EffectContainer;

import java.util.HashMap;
import java.util.Map;

public final class MutableWorldState implements ReadableWorldState, WritableWorldState {

    private final Map<TypedIdentifier<?>, Object> stateMap;

    public MutableWorldState() {
        this(new HashMap<>());
    }

    public MutableWorldState(ReadableWorldState worldState) {
        this(new HashMap<>(worldState.getMap()));
    }

    public MutableWorldState(HashMap<TypedIdentifier<?>, Object> stateMap) {
        this.stateMap = stateMap;
    }

    @Override
    public Map<TypedIdentifier<?>, ?> getMap() {
        return stateMap;
    }

    @Override
    public <T> void set(TypedIdentifier<T> key, T value) {
        stateMap.put(key, value);
    }

    @Override
    public void apply(EffectContainer effectContainer) {
        effectContainer.getEffects().forEach(effect -> effect.apply(this));
    }

    @Override
    public MutableWorldState copy() {
        return new MutableWorldState(this);
    }

    @Override
    public String toString() {
        return "WorldState{" +
            "stateMap=" + getMap() +
            '}';
    }
}
