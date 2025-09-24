package com.just.goap.condition;

import com.just.goap.TypedIdentifier;
import com.just.goap.condition.expression.GOAPExpression;
import com.just.goap.effect.GOAPEffectContainer;
import com.just.goap.state.GOAPWorldState;

public record GOAPCondition<T>(
    TypedIdentifier<? super T> identifier,
    GOAPExpression<? super T> expression
) {

    public boolean satisfiedBy(GOAPEffectContainer effectContainer) {
        return satisfiedBy(effectContainer.toWorldState());
    }

    @SuppressWarnings("unchecked")
    public boolean satisfiedBy(GOAPWorldState worldState) {
        var value = (T) worldState.get(identifier);
        return value != null && expression.evaluate(value);
    }
}
