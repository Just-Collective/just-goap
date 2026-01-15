package com.just.goap.sensor;

import java.util.Set;

import com.just.core.functional.function.Function2;
import com.just.goap.StateKey;
import com.just.goap.state.ReadableWorldState;

public final class LazyDecompose2<T, O1, O2> implements Sensor.Multi<T> {

    private final StateKey.Sensed<O1> outputKeyA;

    private final StateKey.Sensed<O2> outputKeyB;

    private final Function2<? super T, ? super ReadableWorldState, ? extends java.util.Map<StateKey<?>, ?>> extractor;

    LazyDecompose2(
        StateKey.Sensed<O1> outputKeyA,
        StateKey.Sensed<O2> outputKeyB,
        Function2<? super T, ? super ReadableWorldState, ? extends java.util.Map<StateKey<?>, ?>> extractor
    ) {
        this.outputKeyA = outputKeyA;
        this.outputKeyB = outputKeyB;
        this.extractor = extractor;
    }

    @Override
    public Set<StateKey.Sensed<?>> outputKeys() {
        return Set.of(outputKeyA, outputKeyB);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <V> V apply(StateKey<V> key, T actor, ReadableWorldState worldState) {
        return (V) extractor.apply(actor, worldState).get(key);
    }
}
