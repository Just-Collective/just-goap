package com.just.goap.sensor;

import java.util.Set;

import com.just.core.functional.function.Function2;
import com.just.goap.StateKey;
import com.just.goap.state.ReadableWorldState;

public final class Map1To2<T, I1, O1, O2> implements Sensor.Multi<T> {

    private final StateKey.Sensed<I1> sourceKeyA;

    private final StateKey.Sensed<O1> outputKeyA;

    private final StateKey.Sensed<O2> outputKeyB;

    private final Function2<? super T, ? super I1, ? extends java.util.Map<StateKey<?>, ?>> extractor;

    Map1To2(
        StateKey.Sensed<I1> sourceKeyA,
        StateKey.Sensed<O1> outputKeyA,
        StateKey.Sensed<O2> outputKeyB,
        Function2<? super T, ? super I1, ? extends java.util.Map<StateKey<?>, ?>> extractor
    ) {
        this.sourceKeyA = sourceKeyA;
        this.outputKeyA = outputKeyA;
        this.outputKeyB = outputKeyB;
        this.extractor = extractor;
    }

    @Override
    public Set<StateKey.Sensed<?>> outputKeys() {
        return Set.of(outputKeyA, outputKeyB);
    }

    @SuppressWarnings("unchecked")
    public <V> V apply(StateKey<V> key, T actor, ReadableWorldState worldState) {
        var sourceValueA = worldState.getOrNull(sourceKeyA);
        return (V) extractor.apply(actor, sourceValueA).get(key);
    }
}
