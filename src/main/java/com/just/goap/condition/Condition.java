package com.just.goap.condition;

import com.just.goap.TypedIdentifier;
import com.just.goap.condition.expression.Expression;
import com.just.goap.effect.EffectContainer;
import com.just.goap.state.ReadableWorldState;

public record Condition<T>(
    TypedIdentifier<? super T> identifier,
    Expression<? super T> expression
) {

    public boolean satisfiedBy(EffectContainer effectContainer) {
        return satisfiedBy(effectContainer.toWorldState());
    }

    @SuppressWarnings("unchecked")
    public boolean satisfiedBy(ReadableWorldState worldState) {
        var value = (T) worldState.getOrNull(identifier);
        return value != null && expression.evaluate(value);
    }
}
