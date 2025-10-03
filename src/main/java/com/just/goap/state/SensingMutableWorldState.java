package com.just.goap.state;

import com.just.goap.Sensor;
import com.just.goap.StateKey;
import com.just.goap.effect.EffectContainer;

import java.util.HashMap;
import java.util.Map;

public final class SensingMutableWorldState<T> implements WorldState {

    private final T context;

    private final Map<StateKey<?>, Sensor<? super T, ?>> sensorMap;

    private final Map<StateKey<?>, Object> stateMap;

    public SensingMutableWorldState(T context, Map<StateKey<?>, Sensor<? super T, ?>> sensorMap) {
        this(context, sensorMap, new HashMap<>());
    }

    private SensingMutableWorldState(
        T context,
        Map<StateKey<?>, Sensor<? super T, ?>> sensorMap,
        Map<StateKey<?>, Object> stateMap
    ) {
        this.context = context;
        this.sensorMap = sensorMap;
        this.stateMap = stateMap;
    }

    @Override
    public <U> U getOrNull(StateKey<U> key) {
        @SuppressWarnings("unchecked")
        var value = (U) stateMap.get(key);

        if (value == null) {
            @SuppressWarnings("unchecked")
            var sensor = (Sensor<T, U>) sensorMap.get(key);

            switch (sensor) {
                case Sensor.Direct<T, U> direct -> {
                    value = direct.extractor().apply(context);
                    set(key, value);
                }
                case Sensor.Derived<T, U, ?> derived -> {
                    @SuppressWarnings("unchecked")
                    var castedDerived = ((Sensor.Derived<T, U, Object>) derived);
                    var sourceValue = getOrNull(castedDerived.sourceKey());
                    value = castedDerived.extractor().apply(context, sourceValue);
                    set(key, value);
                }
            }
        }

        return value;
    }

    @Override
    public Map<StateKey<?>, ?> getMap() {
        return stateMap;
    }

    @Override
    public <U> void set(StateKey<U> key, U value) {
        stateMap.put(key, value);
    }

    @Override
    public void apply(EffectContainer effectContainer) {
        effectContainer.getEffects().forEach(effect -> effect.apply(this));
    }

    @Override
    public SensingMutableWorldState<T> copy() {
        return new SensingMutableWorldState<>(context, sensorMap, new HashMap<>(stateMap));
    }

    @Override
    public String toString() {
        return "WorldState{" +
            "stateMap=" + getMap() +
            '}';
    }

}
