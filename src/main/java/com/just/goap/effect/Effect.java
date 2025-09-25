package com.just.goap.effect;

import com.just.goap.TypedIdentifier;
import com.just.goap.state.MutableWorldState;

import java.util.function.UnaryOperator;

public sealed interface Effect<T> {

    TypedIdentifier<T> identifier();

    void apply(MutableWorldState worldState);

    record Value<T>(
        TypedIdentifier<T> identifier,
        T value
    ) implements Effect<T> {

        @Override
        public void apply(MutableWorldState worldState) {
            worldState.set(identifier, value);
        }
    }

    record Dynamic<T>(
        TypedIdentifier<T> identifier,
        UnaryOperator<T> consumer
    ) implements Effect<T> {

        @Override
        public void apply(MutableWorldState worldState) {
            var existingValue = worldState.getOrNull(identifier);

            if (existingValue != null) {
                var updatedValue = consumer.apply(existingValue);
                worldState.set(identifier, updatedValue);
            }
        }
    }
}
