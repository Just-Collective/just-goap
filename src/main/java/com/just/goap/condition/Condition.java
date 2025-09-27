package com.just.goap.condition;

import com.just.goap.GOAPKey;
import com.just.goap.condition.expression.Expression;
import com.just.goap.effect.EffectContainer;
import com.just.goap.state.ReadableWorldState;

public record Condition<T>(
    GOAPKey<? super T> key,
    Expression<? super T> expression
) {

    public boolean satisfiedBy(EffectContainer effectContainer) {
        return satisfiedBy(effectContainer.toWorldState());
    }

    @SuppressWarnings("unchecked")
    public boolean satisfiedBy(ReadableWorldState worldState) {
        var value = (T) worldState.getOrNull(key);
        return value != null && expression.evaluate(value);
    }
}
