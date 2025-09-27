package com.just.goap;

import java.util.function.BiFunction;
import java.util.function.Function;

public sealed interface Sensor<T, U> {

    static <T, U> Direct<T, U> direct(TypedIdentifier<U> identifier, Function<T, U> extractor) {
        return new Direct<>(identifier, extractor);
    }

    static <T, U, V> Derived<T, U, V> derived(
        TypedIdentifier<U> identifier,
        TypedIdentifier<V> sourceIdentifier,
        BiFunction<T, V, U> extractor
    ) {
        return new Derived<>(identifier, sourceIdentifier, extractor);
    }

    TypedIdentifier<U> identifier();

    record Direct<T, U>(
        TypedIdentifier<U> identifier,
        Function<T, U> extractor
    ) implements Sensor<T, U> {}

    record Derived<T, U, V>(
        TypedIdentifier<U> identifier,
        TypedIdentifier<V> sourceIdentifier,
        BiFunction<T, V, U> extractor
    ) implements Sensor<T, U> {}
}
