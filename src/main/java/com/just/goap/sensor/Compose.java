package com.just.goap.sensor;

import com.just.core.functional.function.Function2;
import com.just.goap.StateKey;
import com.just.goap.state.ReadableWorldState;

public final class Compose<T, I1, O1> implements Sensor.Mono<T, O1> {

    private final StateKey.Sensed<I1> sourceKeyA;

    private final StateKey.Sensed<O1> outputKeyA;

    private final Function2<? super T, ? super I1, ? extends O1> extractor;

    Compose(
        StateKey.Sensed<I1> sourceKeyA,
        StateKey.Sensed<O1> outputKeyA,
        Function2<? super T, ? super I1, ? extends O1> extractor
    ) {
        this.sourceKeyA = sourceKeyA;
        this.outputKeyA = outputKeyA;
        this.extractor = extractor;
    }

    @Override
    public StateKey.Sensed<O1> key() {
        return outputKeyA;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <V> V apply(StateKey<V> key, T context, ReadableWorldState worldState) {
        var sourceValueA = worldState.getOrNull(sourceKeyA);
        return (V) extractor.apply(context, sourceValueA);
    }
}
