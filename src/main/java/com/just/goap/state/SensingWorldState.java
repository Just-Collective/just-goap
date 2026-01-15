package com.just.goap.state;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import com.just.goap.StateKey;
import com.just.goap.effect.EffectContainer;
import com.just.goap.graph.Graph;

public final class SensingWorldState<T> implements WorldState {

    private static final Logger LOGGER = LoggerFactory.getLogger(SensingWorldState.class);

    private final Graph<T> graph;

    private final Map<StateKey<?>, Object> stateMap;

    private T actor;

    public SensingWorldState(Graph<T> graph) {
        this.graph = graph;
        this.stateMap = new HashMap<>();
    }

    @Override
    public <O> O getOrNull(StateKey<O> key) {
        @SuppressWarnings("unchecked")
        var value = (O) stateMap.get(key);

        if (value == null) {
            var sensor = graph.getSensorMap().get(key);

            if (sensor != null) {
                value = sensor.apply(key, actor, this);
                set(key, value);
            } else {
                LOGGER.warn("Attempted to sense a value for key '{}', but no sensor exists for key '{}'.", key, key);
            }
        }

        return value;
    }

    @Override
    public Map<StateKey<?>, Object> getMap() {
        return stateMap;
    }

    @Override
    public <U> void set(StateKey<U> key, U value) {
        stateMap.put(key, value);
    }

    @Override
    public void setAll(Map<StateKey<?>, Object> map) {
        stateMap.putAll(map);
    }

    @Override
    public void apply(EffectContainer effectContainer) {
        for (var effect : effectContainer.getEffects()) {
            effect.apply(this);
        }
    }

    @Override
    public void clear() {
        stateMap.clear();
    }

    public void setActor(T actor) {
        this.actor = actor;
    }

    @Override
    public String toString() {
        return "WorldState{" +
            "stateMap=" + getMap() +
            '}';
    }

    public Graph<T> getGraph() {
        return graph;
    }
}
