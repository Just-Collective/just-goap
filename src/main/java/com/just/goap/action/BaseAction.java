package com.just.goap.action;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.UnaryOperator;

import com.just.goap.StateKey;
import com.just.goap.condition.Condition;
import com.just.goap.condition.ConditionContainer;
import com.just.goap.condition.expression.Expression;
import com.just.goap.effect.Effect;
import com.just.goap.effect.EffectContainer;
import com.just.goap.state.ReadableWorldState;

public class BaseAction<T> implements Action<T> {

    public static <T> ConcreteBuilder<T> builder(String name) {
        return new ConcreteBuilder<>(name);
    }

    private final ConditionContainer preconditions;

    private final EffectContainer effects;

    private final CostCallback<T> costCallback;

    private final StartCallback<T> startCallback;

    private final PerformCallback<T> performCallback;

    private final FinishCallback<T> finishCallback;

    private final String name;

    protected BaseAction(
        String name,
        ConditionContainer conditionContainer,
        EffectContainer effectContainer,
        CostCallback<T> costCallback,
        StartCallback<T> startCallback,
        PerformCallback<T> performCallback,
        FinishCallback<T> finishCallback
    ) {
        this.name = name;
        this.preconditions = conditionContainer;
        this.effects = effectContainer;
        this.costCallback = costCallback;
        this.startCallback = startCallback;
        this.performCallback = performCallback;
        this.finishCallback = finishCallback;
    }

    public float getCost(T actor, ReadableWorldState worldState) {
        return costCallback.apply(actor, worldState);
    }

    public Signal perform(Context<? extends T> context) {
        return performCallback.accept(context);
    }

    public void onStart(Context<? extends T> context) {
        startCallback.apply(context);
    }

    public void onFinish(Context<? extends T> context) {
        finishCallback.apply(context);
    }

    public ConditionContainer getPreconditionContainer() {
        return preconditions;
    }

    public EffectContainer getEffectContainer() {
        return effects;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return getName();
    }

    public abstract static class Builder<T, B extends Builder<T, B>> {

        protected final List<Condition<?>> preconditions;

        protected final List<Effect<?>> effects;

        protected CostCallback<T> costCallback;

        protected StartCallback<T> startCallback;

        protected PerformCallback<T> performCallback;

        protected FinishCallback<T> finishCallback;

        protected String name;

        protected Builder(String name) {
            this.preconditions = new ArrayList<>();
            this.effects = new ArrayList<>();
            this.name = name;
            this.costCallback = ($1, $2) -> 0;
            this.startCallback = $ -> {};
            this.performCallback = $ -> Signal.CONTINUE;
            this.finishCallback = $ -> {};
        }

        protected abstract B self();

        public <U> B addPrecondition(StateKey.Derived<? extends U> key, Expression<? super U> expression) {
            return addPrecondition(Condition.derived(key, expression));
        }

        public <U> B addPrecondition(StateKey.Sensed<? extends U> key, Expression<? super U> expression) {
            return addPrecondition(Condition.sensed(key, expression));
        }

        public B addPrecondition(Condition<?> condition) {
            preconditions.add(condition);
            return self();
        }

        public <U> B addEffect(StateKey.Derived<U> key, UnaryOperator<U> consumer) {
            return addEffect(new Effect.Dynamic<>(key, consumer));
        }

        public <U> B addEffect(StateKey.Derived<U> key, U value) {
            return addEffect(new Effect.Value<>(key, value));
        }

        public B addEffect(Effect<?> effect) {
            effects.add(effect);
            return self();
        }

        public B withName(String name) {
            this.name = name;
            return self();
        }

        public B withCostCallback(CostCallback<T> costCallback) {
            this.costCallback = costCallback;
            return self();
        }

        public B withStartCallback(StartCallback<T> startCallback) {
            this.startCallback = startCallback;
            return self();
        }

        public B withPerformCallback(PerformCallback<T> performCallback) {
            this.performCallback = performCallback;
            return self();
        }

        public B withFinishCallback(FinishCallback<T> finishCallback) {
            this.finishCallback = finishCallback;
            return self();
        }

        public B apply(UnaryOperator<B> unaryOperator) {
            return unaryOperator.apply(self());
        }

        public BaseAction<T> build() {
            if (effects.isEmpty()) {
                throw new IllegalStateException("Action must have at least one effect.");
            }

            return new BaseAction<>(
                name,
                ConditionContainer.of(Collections.unmodifiableList(preconditions)),
                EffectContainer.of(Collections.unmodifiableList(effects)),
                costCallback,
                startCallback,
                performCallback,
                finishCallback
            );
        }
    }

    public static class ConcreteBuilder<T> extends Builder<T, ConcreteBuilder<T>> {

        protected ConcreteBuilder(String name) {
            super(name);
        }

        @Override
        protected ConcreteBuilder<T> self() {
            return this;
        }
    }
}
