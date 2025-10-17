package com.just.goap.state;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import com.just.goap.StateKey;
import com.just.goap.effect.EffectContainer;
import com.just.goap.sensor.Sensor;

public final class SensingWorldState<T> implements WorldState {

    private static final Logger LOGGER = LoggerFactory.getLogger(SensingWorldState.class);

    private final Map<StateKey<?>, Object> stateMap;

    private T context;

    private Map<StateKey<?>, Sensor<? super T>> sensorMap;

    public SensingWorldState() {
        this(new HashMap<>());
    }

    private SensingWorldState(Map<StateKey<?>, Object> stateMap) {
        this.stateMap = stateMap;
    }

    @Override
    public <O> O getOrNull(StateKey<O> key) {
        @SuppressWarnings("unchecked")
        var value = (O) stateMap.get(key);

        if (value == null) {
            var sensor = sensorMap.get(key);

            if (sensor != null) {
                value = sensor.apply(key, context, this);
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

    public void setSensorMap(Map<StateKey<?>, Sensor<? super T>> sensorMap) {
        this.sensorMap = sensorMap;
    }

    public void setContext(T context) {
        this.context = context;
    }

    @Override
    public SensingWorldState<T> copy() {
        var copy = new SensingWorldState<T>(new HashMap<>(stateMap));
        copy.setContext(context);
        copy.setSensorMap(sensorMap);
        return copy;
    }

    @Override
    public String toString() {
        return "WorldState{" +
            "stateMap=" + getMap() +
            '}';
    }
}
