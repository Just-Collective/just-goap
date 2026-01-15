package com.just.goap.sensor;

import com.just.core.functional.function.Function3;
import com.just.goap.StateKey;
import com.just.goap.state.ReadableWorldState;

public final class Compose2<T, I1, I2, O1> implements Sensor.Mono<T, O1> {

    private final StateKey.Sensed<I1> sourceKeyA;

    private final StateKey.Sensed<I2> sourceKeyB;

    private final StateKey.Sensed<O1> outputKeyA;

    private final Function3<? super T, ? super I1, ? super I2, ? extends O1> extractor;

    Compose2(
        StateKey.Sensed<I1> sourceKeyA,
        StateKey.Sensed<I2> sourceKeyB,
        StateKey.Sensed<O1> outputKeyA,
        Function3<? super T, ? super I1, ? super I2, ? extends O1> extractor
    ) {
        this.sourceKeyA = sourceKeyA;
        this.sourceKeyB = sourceKeyB;
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
        var sourceValueA = worldState.getOrNull(sourceKeyA);
        var sourceValueB = worldState.getOrNull(sourceKeyB);
        return (V) extractor.apply(actor, sourceValueA, sourceValueB);
    }
}
