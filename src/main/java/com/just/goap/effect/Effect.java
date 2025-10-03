package com.just.goap.effect;

import com.just.goap.StateKey;
import com.just.goap.state.WorldState;

import java.util.function.UnaryOperator;

public sealed interface Effect<T> {

    StateKey.Derived<T> key();

    void apply(WorldState worldState);

    record Value<T>(
        StateKey.Derived<T> key,
        T value
    ) implements Effect<T> {

        @Override
        public void apply(WorldState worldState) {
            worldState.set(key, value);
        }
    }

    record Dynamic<T>(
        StateKey.Derived<T> key,
        UnaryOperator<T> consumer
    ) implements Effect<T> {

        @Override
        public void apply(WorldState worldState) {
            var existingValue = worldState.getOrNull(key);

            if (existingValue != null) {
                var updatedValue = consumer.apply(existingValue);
                worldState.set(key, updatedValue);
            }
        }
    }
}
