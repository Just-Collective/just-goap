package com.just.goap.state;

import com.just.goap.GOAPKey;
import com.just.goap.Sensor;
import com.just.goap.effect.EffectContainer;

import java.util.Map;

public final class SensingMutableWorldState<T> implements ReadableWorldState, WritableWorldState {

    private final T context;

    private final Map<GOAPKey<?>, Sensor<? super T, ?>> sensorMap;

    private final MutableWorldState mutableWorldState;

    public SensingMutableWorldState(T context, Map<GOAPKey<?>, Sensor<? super T, ?>> sensorMap) {
        this(context, sensorMap, new MutableWorldState());
    }

    private SensingMutableWorldState(
        T context,
        Map<GOAPKey<?>, Sensor<? super T, ?>> sensorMap,
        MutableWorldState mutableWorldState
    ) {
        this.context = context;
        this.sensorMap = sensorMap;
        this.mutableWorldState = mutableWorldState;
    }

    @Override
    public <U> U getOrNull(GOAPKey<U> key) {
        var value = mutableWorldState.getOrNull(key);

        if (value == null) {
            @SuppressWarnings("unchecked")
            var sensor = (Sensor<T, U>) sensorMap.get(key);

            switch (sensor) {
                case Sensor.Derived<T, U, ?> derived -> {
                    @SuppressWarnings("unchecked")
                    var castedDerived = ((Sensor.Derived<T, U, Object>) derived);
                    var sourceValue = getOrNull(castedDerived.sourceKey());
                    value = castedDerived.extractor().apply(context, sourceValue);
                    set(key, value);
                }
                case Sensor.Direct<T, U> direct -> {
                    value = direct.extractor().apply(context);
                    set(key, value);
                }
            }
        }

        return value;
    }

    @Override
    public Map<GOAPKey<?>, ?> getMap() {
        return mutableWorldState.getMap();
    }

    @Override
    public <U> void set(GOAPKey<U> key, U value) {
        mutableWorldState.set(key, value);
    }

    @Override
    public void apply(EffectContainer effectContainer) {
        mutableWorldState.apply(effectContainer);
    }

    @Override
    public SensingMutableWorldState<T> copy() {
        return new SensingMutableWorldState<>(context, sensorMap, mutableWorldState.copy());
    }

    @Override
    public String toString() {
        return "WorldState{" +
            "stateMap=" + getMap() +
            '}';
    }

}
