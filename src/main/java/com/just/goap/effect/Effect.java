package com.just.goap.effect;

import com.just.goap.GOAPKey;
import com.just.goap.state.MutableWorldState;

import java.util.function.UnaryOperator;

public sealed interface Effect<T> {

    GOAPKey<T> key();

    void apply(MutableWorldState worldState);

    record Value<T>(
        GOAPKey<T> key,
        T value
    ) implements Effect<T> {

        @Override
        public void apply(MutableWorldState worldState) {
            worldState.set(key, value);
        }
    }

    record Dynamic<T>(
        GOAPKey<T> key,
        UnaryOperator<T> consumer
    ) implements Effect<T> {

        @Override
        public void apply(MutableWorldState worldState) {
            var existingValue = worldState.getOrNull(key);

            if (existingValue != null) {
                var updatedValue = consumer.apply(existingValue);
                worldState.set(key, updatedValue);
            }
        }
    }
}
