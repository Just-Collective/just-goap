package com.just.goap.state;

import com.just.goap.StateKey;
import com.just.goap.effect.EffectContainer;
import com.just.goap.sensor.Sensor;

import java.util.HashMap;
import java.util.Map;

public final class SensingMutableWorldState<T> implements WorldState {

    private final T context;

    private final Map<StateKey<?>, Sensor<? super T>> sensorMap;

    private final Map<StateKey<?>, Object> stateMap;

    public SensingMutableWorldState(T context, Map<StateKey<?>, Sensor<? super T>> sensorMap) {
        this(context, sensorMap, new HashMap<>());
    }

    private SensingMutableWorldState(
        T context,
        Map<StateKey<?>, Sensor<? super T>> sensorMap,
        Map<StateKey<?>, Object> stateMap
    ) {
        this.context = context;
        this.sensorMap = sensorMap;
        this.stateMap = stateMap;
    }

    @Override
    public <O> O getOrNull(StateKey<O> key) {
        @SuppressWarnings("unchecked")
        var value = (O) stateMap.get(key);

        if (value == null) {
            @SuppressWarnings("unchecked")
            var sensor = (Sensor<T>) sensorMap.get(key);

            value = switch (sensor) {
                case Sensor.Mono<T, ?> mono -> extractValueFromMonoSensor(key, mono);
                case Sensor.Multi<T> multi -> extractValueFromMultiSensor(key, multi);
            };

            set(key, value);
        }

        return value;
    }

    private <O> O extractValueFromMonoSensor(StateKey<O> key, Sensor.Mono<T, ?> mono) {
        return switch (mono) {
            case Sensor.Mono.LazyCompose<T, ?, ?> lazyCompose -> {
                @SuppressWarnings("unchecked")
                var castedSensor = (Sensor.Mono.LazyCompose<T, ReadableWorldState, Object>) lazyCompose;
                var resultMap = castedSensor.extractor().apply(context, this);
                @SuppressWarnings("unchecked")
                var castedValue = (O) resultMap.get(key);
                yield castedValue;
            }
            case Sensor.Mono.Map<T, ?> map -> {
                @SuppressWarnings("unchecked")
                var castedSensor = (Sensor.Mono.Map<T, O>) map;
                yield castedSensor.extractor().apply(context);
            }
            case Sensor.Mono.Compose<T, ?, ?> compose -> {
                @SuppressWarnings("unchecked")
                var castedSensor = (Sensor.Mono.Compose<T, Object, O>) compose;
                var sourceValue = getOrNull(castedSensor.sourceKey());
                yield castedSensor.extractor().apply(context, sourceValue);
            }
            case Sensor.Mono.Compose2<T, ?, ?, ?> compose2 -> {
                @SuppressWarnings("unchecked")
                var castedSensor = (Sensor.Mono.Compose2<T, Object, Object, O>) compose2;
                var sourceValueA = getOrNull(castedSensor.sourceKeyA());
                var sourceValueB = getOrNull(castedSensor.sourceKeyB());
                yield castedSensor.extractor().apply(context, sourceValueA, sourceValueB);
            }
            case Sensor.Mono.Compose3<T, ?, ?, ?, ?> compose3 -> {
                @SuppressWarnings("unchecked")
                var castedSensor = (Sensor.Mono.Compose3<T, Object, Object, Object, O>) compose3;
                var sourceValueA = getOrNull(castedSensor.sourceKeyA());
                var sourceValueB = getOrNull(castedSensor.sourceKeyB());
                var sourceValueC = getOrNull(castedSensor.sourceKeyC());
                yield castedSensor.extractor().apply(context, sourceValueA, sourceValueB, sourceValueC);
            }
        };
    }

    private <O> O extractValueFromMultiSensor(StateKey<O> key, Sensor.Multi<T> multi) {
        return switch (multi) {
            case Sensor.Multi.Decompose2<T, ?, ?> decompose2 -> {
                @SuppressWarnings("unchecked")
                var castedSensor = (Sensor.Multi.Decompose2<T, Object, Object>) decompose2;
                var resultMap = castedSensor.extractor().apply(context);
                @SuppressWarnings("unchecked")
                var castedValue = (O) resultMap.get(key);
                yield castedValue;
            }
            case Sensor.Multi.Decompose3<T, ?, ?, ?> decompose3 -> {
                @SuppressWarnings("unchecked")
                var castedSensor = (Sensor.Multi.Decompose3<T, Object, Object, Object>) decompose3;
                var resultMap = castedSensor.extractor().apply(context);
                @SuppressWarnings("unchecked")
                var castedValue = (O) resultMap.get(key);
                yield castedValue;
            }
            case Sensor.Multi.LazyDecompose2<T, ?, ?, ?> lazyDecompose2 -> {
                @SuppressWarnings("unchecked")
                var castedSensor =
                    (Sensor.Multi.LazyDecompose2<T, ReadableWorldState, Object, Object>) lazyDecompose2;
                var resultMap = castedSensor.extractor().apply(context, this);
                @SuppressWarnings("unchecked")
                var castedValue = (O) resultMap.get(key);
                yield castedValue;
            }
            case Sensor.Multi.Map1To2<T, ?, ?, ?> map1To2 -> {
                @SuppressWarnings("unchecked")
                var castedSensor = (Sensor.Multi.Map1To2<T, Object, Object, Object>) map1To2;
                var sourceValueA = getOrNull(castedSensor.sourceKeyA());
                var resultMap = castedSensor.extractor().apply(context, sourceValueA);
                @SuppressWarnings("unchecked")
                var castedValue = (O) resultMap.get(key);
                yield castedValue;
            }
            case Sensor.Multi.Map2To2<T, ?, ?, ?, ?> map2To2 -> {
                @SuppressWarnings("unchecked")
                var castedSensor = (Sensor.Multi.Map2To2<T, Object, Object, Object, Object>) map2To2;
                var sourceValueA = getOrNull(castedSensor.sourceKeyA());
                var sourceValueB = getOrNull(castedSensor.sourceKeyB());
                var resultMap = castedSensor.extractor().apply(context, sourceValueA, sourceValueB);
                @SuppressWarnings("unchecked")
                var castedValue = (O) resultMap.get(key);
                yield castedValue;
            }
        };
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
