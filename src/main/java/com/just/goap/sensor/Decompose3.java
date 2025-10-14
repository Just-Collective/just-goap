package com.just.goap.sensor;

import java.util.Set;

import com.just.core.functional.function.Function;
import com.just.goap.StateKey;
import com.just.goap.state.ReadableWorldState;

public final class Decompose3<T, O1, O2, O3> implements Sensor.Multi<T> {

    private final StateKey.Sensed<O1> outputKeyA;

    private final StateKey.Sensed<O2> outputKeyB;

    private final StateKey.Sensed<O3> outputKeyC;

    private final Function<? super T, ? extends java.util.Map<StateKey<?>, ?>> extractor;

    Decompose3(
        StateKey.Sensed<O1> outputKeyA,
        StateKey.Sensed<O2> outputKeyB,
        StateKey.Sensed<O3> outputKeyC,
        Function<? super T, ? extends java.util.Map<StateKey<?>, ?>> extractor
    ) {
        this.outputKeyA = outputKeyA;
        this.outputKeyB = outputKeyB;
        this.outputKeyC = outputKeyC;
        this.extractor = extractor;
    }

    @Override
    public Set<StateKey.Sensed<?>> outputKeys() {
        return Set.of(outputKeyA, outputKeyB, outputKeyC);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <V> V apply(StateKey<V> key, T context, ReadableWorldState worldState) {
        return (V) extractor.apply(context).get(key);
    }
}
