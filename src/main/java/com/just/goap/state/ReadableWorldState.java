package com.just.goap.state;

import com.just.core.functional.option.Option;
import com.just.goap.GOAPKey;
import com.just.goap.Satisfiable;
import com.just.goap.Satisfier;
import com.just.goap.condition.ConditionContainer;
import com.just.goap.effect.EffectContainer;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;

public interface ReadableWorldState extends Satisfiable, Satisfier {

    <T> @Nullable T getOrNull(GOAPKey<T> key);

    Map<GOAPKey<?>, ?> getMap();

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

    default boolean has(GOAPKey<?> key) {
        var value = getOrNull(key);
        return value != null;
    }

    default <T> T getOrDefault(GOAPKey<T> key, T defaultValue) {
        var value = getOrNull(key);
        return value == null
            ? defaultValue
            : value;
    }

    default <T> T getOrThrow(GOAPKey<T> key) {
        return Objects.requireNonNull(getOrNull(key));
    }

    default <T> Option<T> get(GOAPKey<T> key) {
        return Option.ofNullable(getOrNull(key));
    }
}
