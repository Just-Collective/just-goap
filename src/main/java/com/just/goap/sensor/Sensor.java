package com.just.goap.sensor;

import java.util.Map;
import java.util.Set;

import com.just.core.functional.function.Function;
import com.just.core.functional.function.Function2;
import com.just.core.functional.function.Function3;
import com.just.core.functional.function.Function4;
import com.just.core.functional.tuple.Tuple2;
import com.just.core.functional.tuple.Tuple3;
import com.just.goap.StateKey;
import com.just.goap.state.ReadableWorldState;

public sealed interface Sensor<T> {

    static <T, I1, O1> Mono.Compose<T, I1, O1> compose(
        StateKey.Sensed<I1> sourceKey,
        StateKey.Sensed<O1> key,
        Function2<? super T, ? super I1, ? extends O1> extractor
    ) {
        return new Mono.Compose<>(sourceKey, key, extractor);
    }

    static <T, I1, I2, O1> Mono.Compose2<T, I1, I2, O1> compose(
        StateKey.Sensed<I1> sourceKeyA,
        StateKey.Sensed<I2> sourceKeyB,
        StateKey.Sensed<O1> key,
        Function3<? super T, ? super I1, ? super I2, ? extends O1> extractor
    ) {
        return new Mono.Compose2<>(sourceKeyA, sourceKeyB, key, extractor);
    }

    static <T, I1, I2, I3, O1> Mono.Compose3<T, I1, I2, I3, O1> compose(
        StateKey.Sensed<I1> sourceKeyA,
        StateKey.Sensed<I2> sourceKeyB,
        StateKey.Sensed<I3> sourceKeyC,
        StateKey.Sensed<O1> key,
        Function4<? super T, ? super I1, ? super I2, ? super I3, ? extends O1> extractor
    ) {
        return new Mono.Compose3<>(sourceKeyA, sourceKeyB, sourceKeyC, key, extractor);
    }

    static <T, O1> Mono.Map<T, O1> map(StateKey.Sensed<O1> key, Function<? super T, ? extends O1> extractor) {
        return new Mono.Map<>(key, extractor);
    }

    static <T, O1, O2> Multi.Decompose2<T, O1, O2> decompose(
        StateKey.Sensed<O1> outputKeyA,
        StateKey.Sensed<O2> outputKeyB,
        Function<? super T, ? extends Tuple2<O1, O2>> extractor
    ) {
        return new Multi.Decompose2<>(outputKeyA, outputKeyB, context -> {
            var tuple = extractor.apply(context);
            return Map.of(
                outputKeyA,
                tuple.v1(),
                outputKeyB,
                tuple.v2()
            );
        });
    }

    static <T, O1, O2, O3> Multi.Decompose3<T, O1, O2, O3> decompose(
        StateKey.Sensed<O1> outputKeyA,
        StateKey.Sensed<O2> outputKeyB,
        StateKey.Sensed<O3> outputKeyC,
        Function<? super T, ? extends Tuple3<O1, O2, O3>> extractor
    ) {
        return new Multi.Decompose3<>(outputKeyA, outputKeyB, outputKeyC, context -> {
            var tuple = extractor.apply(context);
            return Map.of(
                outputKeyA,
                tuple.v1(),
                outputKeyB,
                tuple.v2(),
                outputKeyC,
                tuple.v3()
            );
        });
    }

    static <T, W extends ReadableWorldState, O1, O2> Multi.LazyDecompose2<T, W, O1, O2> lazyDecompose(
        StateKey.Sensed<O1> outputKeyA,
        StateKey.Sensed<O2> outputKeyB,
        Function2<? super T, ? super W, ? extends Tuple2<O1, O2>> extractor
    ) {
        return new Multi.LazyDecompose2<T, W, O1, O2>(outputKeyA, outputKeyB, (context, worldState) -> {
            var tuple = extractor.apply(context, worldState);
            return Map.of(
                outputKeyA,
                tuple.v1(),
                outputKeyB,
                tuple.v2()
            );
        });
    }

    static <T, I1, O1, O2> Multi.Map1To2<T, I1, O1, O2> multiMap(
        StateKey.Sensed<I1> sourceKeyA,
        StateKey.Sensed<O1> outputKeyA,
        StateKey.Sensed<O2> outputKeyB,
        Function2<? super T, ? super I1, ? extends Tuple2<O1, O2>> extractor
    ) {
        return new Multi.Map1To2<T, I1, O1, O2>(sourceKeyA, outputKeyA, outputKeyB, (context, sourceA) -> {
            var tuple = extractor.apply(context, sourceA);
            return Map.of(
                outputKeyA,
                tuple.v1(),
                outputKeyB,
                tuple.v2()
            );
        });
    }

    static <T, I1, I2, O1, O2> Multi.Map2To2<T, I1, I2, O1, O2> multiMap(
        StateKey.Sensed<I1> sourceKeyA,
        StateKey.Sensed<I2> sourceKeyB,
        StateKey.Sensed<O1> outputKeyA,
        StateKey.Sensed<O2> outputKeyB,
        Function3<? super T, ? super I1, ? super I2, ? extends Tuple2<O1, O2>> extractor
    ) {
        return new Multi.Map2To2<T, I1, I2, O1, O2>(
            sourceKeyA,
            sourceKeyB,
            outputKeyA,
            outputKeyB,
            (context, sourceA, sourceB) -> {
                var tuple = extractor.apply(context, sourceA, sourceB);
                return Map.of(
                    outputKeyA,
                    tuple.v1(),
                    outputKeyB,
                    tuple.v2()
                );
            }
        );
    }

    Set<StateKey.Sensed<?>> outputKeys();

    sealed interface Mono<T, O> extends Sensor<T> {

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

    sealed interface Multi<T> extends Sensor<T> {

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
            Function<? super T, ? extends Map<StateKey<?>, ?>> extractor
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
}
