package com.just.goap.sensor;

import com.just.core.functional.function.Function;
import com.just.goap.StateKey;
import com.just.goap.state.ReadableWorldState;

public final class Map<T, O1> implements Sensor.Mono<T, O1> {

    private final StateKey.Sensed<O1> outputKeyA;

    private final Function<? super T, ? extends O1> extractor;

    Map(StateKey.Sensed<O1> outputKeyA, Function<? super T, ? extends O1> extractor) {
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
        return (V) extractor.apply(actor);
    }
}
