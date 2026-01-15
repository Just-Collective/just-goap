package com.just.goap.state;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.just.core.functional.option.Option;
import com.just.goap.StateKey;

public class Blackboard {

    private final Map<StateKey<?>, Object> stateMap;

    public Blackboard() {
        this.stateMap = new HashMap<>();
    }

    public <T> T getOrNull(StateKey<T> key) {
        return getOrDefault(key, null);
    }

    @SuppressWarnings("unchecked")
    public <T> T getOrDefault(StateKey<T> key, T defaultValue) {
        return (T) stateMap.getOrDefault(key, defaultValue);
    }

    public <T> T getOrThrow(StateKey<T> key) {
        return Objects.requireNonNull(getOrNull(key));
    }

    public <T> Option<T> get(StateKey<T> key) {
        return Option.ofNullable(getOrNull(key));
    }

    public <T> void set(StateKey<T> key, T value) {
        stateMap.put(key, value);
    }

    public void clear() {
        stateMap.clear();
    }

    public enum Scope {
        ACTION,
        AGENT,
        GRAPH,
        PLAN
    }
}
