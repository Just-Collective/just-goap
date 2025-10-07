package com.just.goap.sensor;

import com.just.core.functional.function.Function;
import com.just.core.functional.function.Function2;
import com.just.core.functional.function.Function3;
import com.just.goap.StateKey;
import com.just.goap.state.ReadableWorldState;

import java.util.Map;
import java.util.Set;

public sealed interface Multi<T> extends Sensor<T> {

    record Decompose2<T, O1, O2>(
        StateKey.Sensed<O1> outputKeyA,
        StateKey.Sensed<O2> outputKeyB,
        Function<? super T, ? extends Map<StateKey<?>, ?>> extractor
    ) implements Multi<T> {

        @Override
        public Set<StateKey.Sensed<?>> outputKeys() {
            return Set.of(outputKeyA, outputKeyB);
        }
    }

    record Decompose3<T, O1, O2, O3>(
        StateKey.Sensed<O1> outputKeyA,
        StateKey.Sensed<O2> outputKeyB,
        StateKey.Sensed<O3> outputKeyC,
        Function<? super T, ? extends java.util.Map<StateKey<?>, ?>> extractor
    ) implements Multi<T> {

        @Override
        public Set<StateKey.Sensed<?>> outputKeys() {
            return Set.of(outputKeyA, outputKeyB, outputKeyC);
        }
    }

    record LazyDecompose2<T, W extends ReadableWorldState, O1, O2>(
        StateKey.Sensed<O1> outputKeyA,
        StateKey.Sensed<O2> outputKeyB,
        Function2<? super T, ? super W, ? extends Map<StateKey<?>, ?>> extractor
    ) implements Multi<T> {

        @Override
        public Set<StateKey.Sensed<?>> outputKeys() {
            return Set.of(outputKeyA, outputKeyB);
        }
    }

    record Map1To2<T, I1, O1, O2>(
        StateKey.Sensed<I1> sourceKeyA,
        StateKey.Sensed<O1> outputKeyA,
        StateKey.Sensed<O2> outputKeyB,
        Function2<? super T, ? super I1, ? extends Map<StateKey<?>, ?>> extractor
    ) implements Multi<T> {

        @Override
        public Set<StateKey.Sensed<?>> outputKeys() {
            return Set.of(outputKeyA, outputKeyB);
        }
    }

    record Map2To2<T, I1, I2, O1, O2>(
        StateKey.Sensed<I1> sourceKeyA,
        StateKey.Sensed<I2> sourceKeyB,
        StateKey.Sensed<O1> outputKeyA,
        StateKey.Sensed<O2> outputKeyB,
        Function3<? super T, ? super I1, ? super I2, ? extends Map<StateKey<?>, ?>> extractor
    ) implements Multi<T> {

        @Override
        public Set<StateKey.Sensed<?>> outputKeys() {
            return Set.of(outputKeyA, outputKeyB);
        }
    }
}
