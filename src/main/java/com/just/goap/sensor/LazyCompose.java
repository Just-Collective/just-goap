package com.just.goap.sensor;

import java.util.Map;

import com.just.core.functional.function.Function2;
import com.just.goap.StateKey;
import com.just.goap.state.ReadableWorldState;

public final class LazyCompose<T, O1> implements Sensor.Mono<T, O1> {

    private final StateKey.Sensed<O1> outputKeyA;

    private final Function2<? super T, ? super ReadableWorldState, ? extends Map<StateKey<?>, ?>> extractor;

    LazyCompose(
        StateKey.Sensed<O1> outputKeyA,
        Function2<? super T, ? super ReadableWorldState, ? extends Map<StateKey<?>, ?>> extractor
    ) {
        this.outputKeyA = outputKeyA;
        this.extractor = extractor;
    }

    @Override
    public StateKey.Sensed<O1> key() {
        return outputKeyA;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <V> V apply(StateKey<V> key, T actor, ReadableWorldState worldState) {
        return (V) extractor.apply(actor, worldState).get(key);
    }
}
