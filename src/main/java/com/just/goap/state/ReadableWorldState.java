package com.just.goap.state;

import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;

import com.just.core.functional.option.Option;
import com.just.goap.Satisfiable;
import com.just.goap.Satisfier;
import com.just.goap.StateKey;
import com.just.goap.condition.ConditionContainer;
import com.just.goap.effect.EffectContainer;

public interface ReadableWorldState extends Satisfiable, Satisfier {

    <T> @Nullable T getOrNull(StateKey<T> key);

    Map<StateKey<?>, ?> getMap();

    @Override
    default boolean satisfiedBy(ReadableWorldState worldState) {
        for (var entry : getMap().entrySet()) {
            var otherValue = worldState.getOrNull(entry.getKey());

            if (!entry.getValue().equals(otherValue)) {
                return false;
            }
        }

        return true;
    }

    @Override
    default boolean satisfies(EffectContainer effectContainer) {
        return effectContainer.toWorldState()
            .satisfiedBy(this);
    }

    @Override
    default boolean satisfies(ConditionContainer conditionContainer) {
        return conditionContainer.satisfiedBy(this);
    }

    default boolean has(StateKey<?> key) {
        var value = getOrNull(key);
        return value != null;
    }

    default <T> T getOrDefault(StateKey<T> key, T defaultValue) {
        var value = getOrNull(key);
        return value == null
            ? defaultValue
            : value;
    }

    default <T> T getOrThrow(StateKey<T> key) {
        return Objects.requireNonNull(getOrNull(key));
    }

    default <T> Option<T> get(StateKey<T> key) {
        return Option.ofNullable(getOrNull(key));
    }
}
