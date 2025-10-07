package com.just.goap.sensor;

import com.just.core.functional.function.Function;
import com.just.core.functional.function.Function2;
import com.just.core.functional.function.Function3;
import com.just.core.functional.function.Function4;
import com.just.goap.StateKey;
import com.just.goap.state.ReadableWorldState;

import java.util.Set;

public sealed interface Mono<T, O> extends Sensor<T> {

    StateKey.Sensed<O> key();

    @Override
    default Set<StateKey.Sensed<?>> outputKeys() {
        return Set.of(key());
    }

    record Map<T, O1>(
        StateKey.Sensed<O1> key,
        Function<? super T, ? extends O1> extractor
    ) implements Mono<T, O1> {}

    record Compose<T, I1, O1>(
        StateKey.Sensed<I1> sourceKey,
        StateKey.Sensed<O1> key,
        Function2<? super T, ? super I1, ? extends O1> extractor
    ) implements Mono<T, O1> {}

    record Compose2<T, I1, I2, O1>(
        StateKey.Sensed<I1> sourceKeyA,
        StateKey.Sensed<I2> sourceKeyB,
        StateKey.Sensed<O1> key,
        Function3<? super T, ? super I1, ? super I2, ? extends O1> extractor
    ) implements Mono<T, O1> {}

    record Compose3<T, I1, I2, I3, O1>(
        StateKey.Sensed<I1> sourceKeyA,
        StateKey.Sensed<I2> sourceKeyB,
        StateKey.Sensed<I3> sourceKeyC,
        StateKey.Sensed<O1> key,
        Function4<? super T, ? super I1, ? super I2, ? super I3, ? extends O1> extractor
    ) implements Mono<T, O1> {}

    record LazyCompose<T, W extends ReadableWorldState, O1>(
        StateKey.Sensed<O1> outputKeyA,
        Function2<? super T, ? super W, ? extends java.util.Map<StateKey<?>, ?>> extractor
    ) implements Mono<T, O1> {

        @Override
        public StateKey.Sensed<O1> key() {
            return outputKeyA;
        }
    }
}
