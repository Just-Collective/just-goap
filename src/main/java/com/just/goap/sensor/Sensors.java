package com.just.goap.sensor;

import com.just.core.functional.function.Function;
import com.just.core.functional.function.Function2;
import com.just.core.functional.function.Function3;
import com.just.core.functional.function.Function4;
import com.just.core.functional.tuple.Tuple2;
import com.just.core.functional.tuple.Tuple3;
import com.just.goap.StateKey;
import com.just.goap.state.ReadableWorldState;

public class Sensors {

    public static <T, I1, O1> Compose<T, I1, O1> compose(
        StateKey.Sensed<I1> sourceKey,
        StateKey.Sensed<O1> key,
        Function2<? super T, ? super I1, ? extends O1> extractor
    ) {
        return new Compose<>(sourceKey, key, extractor);
    }

    public static <T, I1, I2, O1> Compose2<T, I1, I2, O1> compose(
        StateKey.Sensed<I1> sourceKeyA,
        StateKey.Sensed<I2> sourceKeyB,
        StateKey.Sensed<O1> key,
        Function3<? super T, ? super I1, ? super I2, ? extends O1> extractor
    ) {
        return new Compose2<>(sourceKeyA, sourceKeyB, key, extractor);
    }

    public static <T, I1, I2, I3, O1> Compose3<T, I1, I2, I3, O1> compose(
        StateKey.Sensed<I1> sourceKeyA,
        StateKey.Sensed<I2> sourceKeyB,
        StateKey.Sensed<I3> sourceKeyC,
        StateKey.Sensed<O1> key,
        Function4<? super T, ? super I1, ? super I2, ? super I3, ? extends O1> extractor
    ) {
        return new Compose3<>(sourceKeyA, sourceKeyB, sourceKeyC, key, extractor);
    }

    public static <T, O1, O2> Decompose2<T, O1, O2> decompose(
        StateKey.Sensed<O1> outputKeyA,
        StateKey.Sensed<O2> outputKeyB,
        Function<? super T, ? extends Tuple2<O1, O2>> extractor
    ) {
        return new Decompose2<T, O1, O2>(outputKeyA, outputKeyB, actor -> {
            var tuple = extractor.apply(actor);
            return java.util.Map.of(
                outputKeyA,
                tuple.v1(),
                outputKeyB,
                tuple.v2()
            );
        });
    }

    public static <T, O1, O2, O3> Decompose3<T, O1, O2, O3> decompose(
        StateKey.Sensed<O1> outputKeyA,
        StateKey.Sensed<O2> outputKeyB,
        StateKey.Sensed<O3> outputKeyC,
        Function<? super T, ? extends Tuple3<O1, O2, O3>> extractor
    ) {
        return new Decompose3<T, O1, O2, O3>(outputKeyA, outputKeyB, outputKeyC, actor -> {
            var tuple = extractor.apply(actor);
            return java.util.Map.of(
                outputKeyA,
                tuple.v1(),
                outputKeyB,
                tuple.v2(),
                outputKeyC,
                tuple.v3()
            );
        });
    }

    public static <T, O1> LazyCompose<T, O1> lazyCompose(
        StateKey.Sensed<O1> outputKeyA,
        Function2<? super T, ? super ReadableWorldState, ? extends O1> extractor
    ) {
        return new LazyCompose<T, O1>(outputKeyA, (actor, worldState) -> {
            var value = extractor.apply(actor, worldState);
            return java.util.Map.of(outputKeyA, value);
        });
    }

    public static <T, O1, O2> LazyDecompose2<T, O1, O2> lazyDecompose(
        StateKey.Sensed<O1> outputKeyA,
        StateKey.Sensed<O2> outputKeyB,
        Function2<? super T, ? super ReadableWorldState, ? extends Tuple2<O1, O2>> extractor
    ) {
        return new LazyDecompose2<T, O1, O2>(outputKeyA, outputKeyB, (actor, worldState) -> {
            var tuple = extractor.apply(actor, worldState);
            return java.util.Map.of(
                outputKeyA,
                tuple.v1(),
                outputKeyB,
                tuple.v2()
            );
        });
    }

    public static <T, O1> com.just.goap.sensor.Map<T, O1> map(
        StateKey.Sensed<O1> key,
        Function<? super T, ? extends O1> extractor
    ) {
        return new com.just.goap.sensor.Map<>(key, extractor);
    }

    public static <T, I1, O1, O2> Map1To2<T, I1, O1, O2> multiMap(
        StateKey.Sensed<I1> sourceKeyA,
        StateKey.Sensed<O1> outputKeyA,
        StateKey.Sensed<O2> outputKeyB,
        Function2<? super T, ? super I1, ? extends Tuple2<O1, O2>> extractor
    ) {
        return new Map1To2<T, I1, O1, O2>(sourceKeyA, outputKeyA, outputKeyB, (actor, sourceA) -> {
            var tuple = extractor.apply(actor, sourceA);
            return java.util.Map.of(
                outputKeyA,
                tuple.v1(),
                outputKeyB,
                tuple.v2()
            );
        });
    }

    public static <T, I1, I2, O1, O2> Map2To2<T, I1, I2, O1, O2> multiMap(
        StateKey.Sensed<I1> sourceKeyA,
        StateKey.Sensed<I2> sourceKeyB,
        StateKey.Sensed<O1> outputKeyA,
        StateKey.Sensed<O2> outputKeyB,
        Function3<? super T, ? super I1, ? super I2, ? extends Tuple2<O1, O2>> extractor
    ) {
        return new Map2To2<T, I1, I2, O1, O2>(
            sourceKeyA,
            sourceKeyB,
            outputKeyA,
            outputKeyB,
            (actor, sourceA, sourceB) -> {
                var tuple = extractor.apply(actor, sourceA, sourceB);
                return java.util.Map.of(
                    outputKeyA,
                    tuple.v1(),
                    outputKeyB,
                    tuple.v2()
                );
            }
        );
    }
}
