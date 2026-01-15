package com.just.goap.sensor;

import java.util.Set;

import com.just.core.functional.function.Function3;
import com.just.goap.StateKey;
import com.just.goap.state.ReadableWorldState;

public final class Map2To2<T, I1, I2, O1, O2> implements Sensor.Multi<T> {

    private final StateKey.Sensed<I1> sourceKeyA;

    private final StateKey.Sensed<I2> sourceKeyB;

    private final StateKey.Sensed<O1> outputKeyA;

    private final StateKey.Sensed<O2> outputKeyB;

    private final Function3<? super T, ? super I1, ? super I2, ? extends java.util.Map<StateKey<?>, ?>> extractor;

    Map2To2(
        StateKey.Sensed<I1> sourceKeyA,
        StateKey.Sensed<I2> sourceKeyB,
        StateKey.Sensed<O1> outputKeyA,
        StateKey.Sensed<O2> outputKeyB,
        Function3<? super T, ? super I1, ? super I2, ? extends java.util.Map<StateKey<?>, ?>> extractor
    ) {
        this.sourceKeyA = sourceKeyA;
        this.sourceKeyB = sourceKeyB;
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
        var sourceValueB = worldState.getOrNull(sourceKeyB);
        return (V) extractor.apply(actor, sourceValueA, sourceValueB).get(key);
    }
}
