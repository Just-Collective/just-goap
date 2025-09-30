package com.just.goap.condition;

import com.just.goap.GOAPKey;
import com.just.goap.Satisfiable;
import com.just.goap.condition.expression.Expression;
import com.just.goap.effect.EffectContainer;
import com.just.goap.state.ReadableWorldState;

public record Condition<T>(
    GOAPKey<T> key,
    Expression<? super T> expression
) implements Satisfiable {

    @Override
    public boolean satisfiedBy(EffectContainer effectContainer) {
        return satisfiedBy(effectContainer.toWorldState());
    }

    @Override
    public boolean satisfiedBy(ReadableWorldState worldState) {
        var value = worldState.getOrNull(key);
        return value != null && expression.evaluate(value);
    }
}
