package com.just.goap;

import java.util.function.BiFunction;
import java.util.function.Function;

public sealed interface Sensor<T, U> {

    static <T, U> Direct<T, U> direct(GOAPKey.Sensed<U> key, Function<? super T, ? extends U> extractor) {
        return new Direct<>(key, extractor);
    }

    static <T, U, V> Derived<T, U, V> derived(
        GOAPKey.Sensed<U> key,
        GOAPKey.Sensed<V> sourceKey,
        BiFunction<? super T, ? super V, ? extends U> extractor
    ) {
        return new Derived<>(key, sourceKey, extractor);
    }

    GOAPKey.Sensed<U> key();

    record Direct<T, U>(
        GOAPKey.Sensed<U> key,
        Function<? super T, ? extends U> extractor
    ) implements Sensor<T, U> {}

    record Derived<T, U, V>(
        GOAPKey.Sensed<U> key,
        GOAPKey.Sensed<V> sourceKey,
        BiFunction<? super T, ? super V, ? extends U> extractor
    ) implements Sensor<T, U> {}
}
