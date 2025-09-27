package com.just.goap.state;

import com.just.goap.GOAPKey;
import com.just.goap.condition.ConditionContainer;
import com.just.goap.effect.EffectContainer;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public interface ReadableWorldState {

    Map<GOAPKey<?>, ?> getMap();

    @SuppressWarnings("unchecked")
    default <T> @Nullable T getOrNull(GOAPKey<T> key) {
        return (T) getMap().get(key);
    }

    default boolean satisfiedBy(ReadableWorldState worldState) {
        for (var entry : getMap().entrySet()) {
            var otherValue = worldState.getOrNull(entry.getKey());

            if (!entry.getValue().equals(otherValue)) {
                return false;
            }
        }

        return true;
    }

    default boolean satisfies(EffectContainer effectContainer) {
        return effectContainer.toWorldState()
            .satisfiedBy(this);
    }

    default boolean satisfies(ConditionContainer conditionContainer) {
        return conditionContainer.satisfiedBy(this);
    }
}
