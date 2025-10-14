package com.just.goap.sensor;

import com.just.core.functional.function.Function4;
import com.just.goap.StateKey;
import com.just.goap.state.ReadableWorldState;

public final class Compose3<T, I1, I2, I3, O1> implements Sensor.Mono<T, O1> {

    private final StateKey.Sensed<I1> sourceKeyA;

    private final StateKey.Sensed<I2> sourceKeyB;

    private final StateKey.Sensed<I3> sourceKeyC;

    private final StateKey.Sensed<O1> outputKeyA;

    private final Function4<? super T, ? super I1, ? super I2, ? super I3, ? extends O1> extractor;

    Compose3(
        StateKey.Sensed<I1> sourceKeyA,
        StateKey.Sensed<I2> sourceKeyB,
        StateKey.Sensed<I3> sourceKeyC,
        StateKey.Sensed<O1> outputKeyA,
        Function4<? super T, ? super I1, ? super I2, ? super I3, ? extends O1> extractor
    ) {
        this.sourceKeyA = sourceKeyA;
        this.sourceKeyB = sourceKeyB;
        this.sourceKeyC = sourceKeyC;
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
        var sourceValueB = worldState.getOrNull(sourceKeyB);
        var sourceValueC = worldState.getOrNull(sourceKeyC);
        return (V) extractor.apply(context, sourceValueA, sourceValueB, sourceValueC);
    }
}
