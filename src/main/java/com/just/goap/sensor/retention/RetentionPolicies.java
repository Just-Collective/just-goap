package com.just.goap.sensor.retention;

import java.util.Arrays;
import java.util.Objects;

import com.just.core.functional.function.predicate.Predicate3;
import com.just.goap.StateKey;
import com.just.goap.state.ReadableWorldState;

public class RetentionPolicies {

    public static <T, V> RetentionPolicy<T, V> recomputeWhenChanged(StateKey<V> key, StateKey<?> sourceKeyA) {
        return of(key, (context, previousWorldState, currentWorldState) -> {
            var previousValue = previousWorldState.getOrNull(sourceKeyA);
            var currentValue = currentWorldState.getOrNull(sourceKeyA);
            return !Objects.equals(previousValue, currentValue);
        });
    }

    public static <T, V> RetentionPolicy<T, V> recomputeWhenChanged(StateKey<V> key, StateKey<?>... sourceKeys) {
        @SuppressWarnings("unchecked")
        RetentionPolicy<T, V>[] policies = Arrays.stream(sourceKeys)
            .map(sourceKey -> recomputeWhenChanged(key, sourceKey))
            .toArray(RetentionPolicy[]::new);

        return combine(key, policies);
    }

    @SafeVarargs
    public static <T, V> RetentionPolicy<T, V> combine(StateKey<V> key, RetentionPolicy<T, V>... policies) {
        return of(
            key,
            (context, previousWorldState, currentWorldState) -> Arrays.stream(policies)
                .anyMatch(p -> p.shouldRecompute(context, previousWorldState, currentWorldState))
        );
    }

    public static <T, V> RetentionPolicy<T, V> of(
        StateKey<V> key,
        Predicate3<T, ReadableWorldState, ReadableWorldState> predicate3
    ) {
        return new RetentionPolicy<>() {

            @Override
            public StateKey<V> key() {
                return key;
            }

            @Override
            public boolean shouldRecompute(
                T context,
                ReadableWorldState previousWorldState,
                ReadableWorldState currentWorldState
            ) {
                return predicate3.apply(context, previousWorldState, currentWorldState);
            }
        };
    }

    private RetentionPolicies() {
        throw new UnsupportedOperationException();
    }
}
