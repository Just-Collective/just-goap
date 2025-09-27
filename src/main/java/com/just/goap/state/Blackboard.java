package com.just.goap.state;

import com.just.goap.TypedIdentifier;

import java.util.HashMap;
import java.util.Map;

public class Blackboard {

    private final Map<TypedIdentifier<?>, Object> stateMap;

    public Blackboard() {
        this.stateMap = new HashMap<>();
    }

    public <T> T getOrNull(TypedIdentifier<T> typedIdentifier) {
        return getOrDefault(typedIdentifier, null);
    }

    @SuppressWarnings("unchecked")
    public <T> T getOrDefault(TypedIdentifier<T> typedIdentifier, T defaultValue) {
        return (T) stateMap.getOrDefault(typedIdentifier, defaultValue);
    }

    public <T> void set(TypedIdentifier<T> key, T value) {
        stateMap.put(key, value);
    }
}
