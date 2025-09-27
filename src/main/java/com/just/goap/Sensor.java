package com.just.goap;

import java.util.function.BiFunction;
import java.util.function.Function;

public sealed interface Sensor<T, U> {

    static <T, U> Direct<T, U> direct(GOAPKey<U> key, Function<T, U> extractor) {
        return new Direct<>(key, extractor);
    }

    static <T, U, V> Derived<T, U, V> derived(
        GOAPKey<U> key,
        GOAPKey<V> sourceKey,
        BiFunction<T, V, U> extractor
    ) {
        return new Derived<>(key, sourceKey, extractor);
    }

    GOAPKey<U> key();

    record Direct<T, U>(
        GOAPKey<U> key,
        Function<T, U> extractor
    ) implements Sensor<T, U> {}

    record Derived<T, U, V>(
        GOAPKey<U> key,
        GOAPKey<V> sourceKey,
        BiFunction<T, V, U> extractor
    ) implements Sensor<T, U> {}
}
