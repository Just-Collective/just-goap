package com.just.ai.goap.sensor;

import java.util.Set;

import com.just.ai.goap.StateKey;
import com.just.ai.goap.state.ReadableWorldState;
import com.just.core.functional.function.Function;

public final class Decompose2<T, O1, O2> implements Sensor.Multi<T> {

    StateKey.Sensed<O1> outputKeyA;

    StateKey.Sensed<O2> outputKeyB;

    Function<? super T, ? extends java.util.Map<StateKey<?>, ?>> extractor;

    Decompose2(
        StateKey.Sensed<O1> outputKeyA,
        StateKey.Sensed<O2> outputKeyB,
        Function<? super T, ? extends java.util.Map<StateKey<?>, ?>> extractor
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
        return (V) extractor.apply(actor).get(key);
    }
}
