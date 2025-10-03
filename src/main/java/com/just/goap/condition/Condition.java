package com.just.goap.condition;

import com.just.goap.GOAPKey;
import com.just.goap.Satisfiable;
import com.just.goap.condition.expression.Expression;
import com.just.goap.effect.EffectContainer;
import com.just.goap.state.ReadableWorldState;

public sealed abstract class Condition<T> implements Satisfiable {

    public static <T> Condition.Derived<T> derived(GOAPKey.Derived<T> key, Expression<? super T> expression) {
        return new Condition.Derived<>(key, expression);
    }

    public static <T> Condition.Sensed<T> sensed(GOAPKey.Sensed<T> key, Expression<? super T> expression) {
        return new Condition.Sensed<>(key, expression);
    }

    private final GOAPKey<T> key;

    private final Expression<? super T> expression;

    protected Condition(GOAPKey<T> key, Expression<? super T> expression) {
        this.key = key;
        this.expression = expression;
    }

    @Override
    public boolean satisfiedBy(EffectContainer effectContainer) {
        return satisfiedBy(effectContainer.toWorldState());
    }

    @Override
    public boolean satisfiedBy(ReadableWorldState worldState) {
        var value = worldState.getOrNull(key);
        return value != null && expression.evaluate(value);
    }

    public GOAPKey<T> key() {
        return key;
    }

    @Override
    public String toString() {
        return key.id() + " " + expression;
    }

    public static final class Derived<T> extends Condition<T> {

        private Derived(GOAPKey.Derived<T> key, Expression<? super T> expression) {
            super(key, expression);
        }
    }

    public static final class Sensed<T> extends Condition<T> {

        private Sensed(GOAPKey.Sensed<T> key, Expression<? super T> expression) {
            super(key, expression);
        }
    }
}
