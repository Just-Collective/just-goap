package com.just.goap;

import com.just.goap.condition.Condition;
import com.just.goap.condition.ConditionContainer;
import com.just.goap.condition.expression.Expression;
import com.just.goap.effect.Effect;
import com.just.goap.effect.EffectContainer;
import com.just.goap.state.Blackboard;
import com.just.goap.state.ReadableWorldState;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.UnaryOperator;

public class Action<T> {

    public static <T> Builder<T> builder(String name) {
        return new Builder<>(name);
    }

    private final ConditionContainer preconditions;

    private final EffectContainer effects;

    private final CostCallback<T> costCallback;

    private final PerformPredicate<T> performPredicate;

    private final FinishCallback<T> finishCallback;

    private final String name;

    private Action(
        String name,
        ConditionContainer conditionContainer,
        EffectContainer effectContainer,
        CostCallback<T> costCallback,
        PerformPredicate<T> performPredicate,
        FinishCallback<T> finishCallback
    ) {
        this.name = name;
        this.preconditions = conditionContainer;
        this.effects = effectContainer;
        this.costCallback = costCallback;
        this.performPredicate = performPredicate;
        this.finishCallback = finishCallback;
    }

    public float getCost(T context, ReadableWorldState worldState) {
        return costCallback.apply(context, worldState);
    }

    public boolean perform(T context, ReadableWorldState worldState, Blackboard blackboard) {
        return performPredicate.accept(context, worldState, blackboard);
    }

    public void onFinish(T context, ReadableWorldState worldState, Blackboard blackboard) {
        finishCallback.apply(context, worldState, blackboard);
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

    public static class Builder<T> {

        private final List<Condition<?>> preconditions;

        private final List<Effect<?>> effects;

        private CostCallback<T> costCallback;

        private PerformPredicate<T> performPredicate;

        private FinishCallback<T> finishCallback;

        private String name;

        private Builder(String name) {
            this.preconditions = new ArrayList<>();
            this.effects = new ArrayList<>();
            this.name = name;
            this.costCallback = ($1, $2) -> 0;
            this.performPredicate = ($1, $2, $3) -> true;
            this.finishCallback = ($1, $2, $3) -> {};
        }

        public <U> Builder<T> addPrecondition(GOAPKey.Derived<? extends U> key, Expression<? super U> expression) {
            return addPrecondition(Condition.derived(key, expression));
        }

        public <U> Builder<T> addPrecondition(GOAPKey.Sensed<? extends U> key, Expression<? super U> expression) {
            return addPrecondition(Condition.sensed(key, expression));
        }

        public Builder<T> addPrecondition(Condition<?> condition) {
            preconditions.add(condition);
            return this;
        }

        public <U> Builder<T> addEffect(GOAPKey.Derived<U> key, UnaryOperator<U> consumer) {
            return addEffect(new Effect.Dynamic<>(key, consumer));
        }

        public <U> Builder<T> addEffect(GOAPKey.Derived<U> key, U value) {
            return addEffect(new Effect.Value<>(key, value));
        }

        public Builder<T> addEffect(Effect<?> effect) {
            effects.add(effect);
            return this;
        }

        public Builder<T> withName(String name) {
            this.name = name;
            return this;
        }

        public Builder<T> withCostCallback(CostCallback<T> costCallback) {
            this.costCallback = costCallback;
            return this;
        }

        public Builder<T> withPerformPredicate(PerformPredicate<T> performPredicate) {
            this.performPredicate = performPredicate;
            return this;
        }

        public Builder<T> withFinishCallback(FinishCallback<T> finishCallback) {
            this.finishCallback = finishCallback;
            return this;
        }

        public Action<T> build() {
            if (effects.isEmpty()) {
                throw new IllegalStateException("Action must have at least one effect.");
            }

            return new Action<>(
                name,
                ConditionContainer.of(Collections.unmodifiableList(preconditions)),
                EffectContainer.of(Collections.unmodifiableList(effects)),
                costCallback,
                performPredicate,
                finishCallback
            );
        }
    }

    @FunctionalInterface
    public interface CostCallback<T> {

        float apply(T context, ReadableWorldState worldState);
    }

    @FunctionalInterface
    public interface PerformPredicate<T> {

        boolean accept(T context, ReadableWorldState worldState, Blackboard blackboard);
    }

    @FunctionalInterface
    public interface FinishCallback<T> {

        void apply(T context, ReadableWorldState worldState, Blackboard blackboard);
    }
}
