package com.just.goap.state;

import com.just.goap.GOAPKey;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Blackboard {

    private final Map<GOAPKey<?>, Object> stateMap;

    public Blackboard() {
        this.stateMap = new HashMap<>();
    }

    public <T> T getOrNull(GOAPKey<T> key) {
        return getOrDefault(key, null);
    }

    @SuppressWarnings("unchecked")
    public <T> T getOrDefault(GOAPKey<T> key, T defaultValue) {
        return (T) stateMap.getOrDefault(key, defaultValue);
    }

    public <T> T getOrThrow(GOAPKey<T> key) {
        return Objects.requireNonNull(getOrNull(key));
    }

    public <T> void set(GOAPKey<T> key, T value) {
        stateMap.put(key, value);
    }
}
