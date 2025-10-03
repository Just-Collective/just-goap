package com.just.goap;

import java.util.function.BiFunction;
import java.util.function.Function;

public sealed interface Sensor<T, U> {

    static <T, U> Direct<T, U> direct(StateKey.Sensed<U> key, Function<? super T, ? extends U> extractor) {
        return new Direct<>(key, extractor);
    }

    static <T, U, V> Derived<T, U, V> derived(
        StateKey.Sensed<U> key,
        StateKey.Sensed<V> sourceKey,
        BiFunction<? super T, ? super V, ? extends U> extractor
    ) {
        return new Derived<>(key, sourceKey, extractor);
    }

    StateKey.Sensed<U> key();

    record Direct<T, U>(
        StateKey.Sensed<U> key,
        Function<? super T, ? extends U> extractor
    ) implements Sensor<T, U> {}

    record Derived<T, U, V>(
        StateKey.Sensed<U> key,
        StateKey.Sensed<V> sourceKey,
        BiFunction<? super T, ? super V, ? extends U> extractor
    ) implements Sensor<T, U> {}
}
