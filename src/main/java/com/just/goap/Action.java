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

    private final PerformCallback<T> performCallback;

    private final FinishCallback<T> finishCallback;

    private final String name;

    private Action(
        String name,
        ConditionContainer conditionContainer,
        EffectContainer effectContainer,
        CostCallback<T> costCallback,
        PerformCallback<T> performCallback,
        FinishCallback<T> finishCallback
    ) {
        this.name = name;
        this.preconditions = conditionContainer;
        this.effects = effectContainer;
        this.costCallback = costCallback;
        this.performCallback = performCallback;
        this.finishCallback = finishCallback;
    }

    public float getCost(T context, ReadableWorldState worldState) {
        return costCallback.apply(context, worldState);
    }

    public Result perform(T context, ReadableWorldState worldState, Blackboard blackboard) {
        return performCallback.accept(context, worldState, blackboard);
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

        private PerformCallback<T> performCallback;

        private FinishCallback<T> finishCallback;

        private String name;

        private Builder(String name) {
            this.preconditions = new ArrayList<>();
            this.effects = new ArrayList<>();
            this.name = name;
            this.costCallback = ($1, $2) -> 0;
            this.performCallback = ($1, $2, $3) -> Result.FINISHED;
            this.finishCallback = ($1, $2, $3) -> {};
        }

        public <U> Builder<T> addPrecondition(StateKey.Derived<? extends U> key, Expression<? super U> expression) {
            return addPrecondition(Condition.derived(key, expression));
        }

        public <U> Builder<T> addPrecondition(StateKey.Sensed<? extends U> key, Expression<? super U> expression) {
            return addPrecondition(Condition.sensed(key, expression));
        }

        public Builder<T> addPrecondition(Condition<?> condition) {
            preconditions.add(condition);
            return this;
        }

        public <U> Builder<T> addEffect(StateKey.Derived<U> key, UnaryOperator<U> consumer) {
            return addEffect(new Effect.Dynamic<>(key, consumer));
        }

        public <U> Builder<T> addEffect(StateKey.Derived<U> key, U value) {
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

        public Builder<T> withPerformCallback(PerformCallback<T> performCallback) {
            this.performCallback = performCallback;
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
                performCallback,
                finishCallback
            );
        }
    }

    public enum Result {

        CONTINUE(false),
        FAILED(true),
        FINISHED(true);

        private final boolean isTerminating;

        Result(boolean isTerminating) {
            this.isTerminating = isTerminating;
        }

        public boolean isTerminating() {
            return isTerminating;
        }
    }

    @FunctionalInterface
    public interface CostCallback<T> {

        float apply(T context, ReadableWorldState worldState);
    }

    @FunctionalInterface
    public interface PerformCallback<T> {

        Result accept(T context, ReadableWorldState worldState, Blackboard blackboard);
    }

    @FunctionalInterface
    public interface FinishCallback<T> {

        void apply(T context, ReadableWorldState worldState, Blackboard blackboard);
    }
}
