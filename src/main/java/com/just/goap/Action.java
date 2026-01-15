package com.just.goap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.UnaryOperator;

import com.just.goap.condition.Condition;
import com.just.goap.condition.ConditionContainer;
import com.just.goap.condition.expression.Expression;
import com.just.goap.effect.Effect;
import com.just.goap.effect.EffectContainer;
import com.just.goap.plan.Plan;
import com.just.goap.state.Blackboard;
import com.just.goap.state.ReadableWorldState;

public class Action<T> {

    public static <T> Builder<T> builder(String name) {
        return new Builder<>(name);
    }

    private final ConditionContainer preconditions;

    private final EffectContainer effects;

    private final CostCallback<T> costCallback;

    private final StartCallback<T> startCallback;

    private final PerformCallback<T> performCallback;

    private final FinishCallback<T> finishCallback;

    private final String name;

    private Action(
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

    public Signal perform(Context<T> context) {
        return performCallback.accept(context);
    }

    public void onStart(Context<T> context) {
        startCallback.apply(context);
    }

    public void onFinish(Context<T> context) {
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

    public static class Builder<T> {

        private final List<Condition<?>> preconditions;

        private final List<Effect<?>> effects;

        private CostCallback<T> costCallback;

        private StartCallback<T> startCallback;

        private PerformCallback<T> performCallback;

        private FinishCallback<T> finishCallback;

        private String name;

        private Builder(String name) {
            this.preconditions = new ArrayList<>();
            this.effects = new ArrayList<>();
            this.name = name;
            this.costCallback = ($1, $2) -> 0;
            this.startCallback = $ -> {};
            this.performCallback = $ -> Signal.CONTINUE;
            this.finishCallback = $ -> {};
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

        public Builder<T> withStartCallback(StartCallback<T> startCallback) {
            this.startCallback = startCallback;
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
                startCallback,
                performCallback,
                finishCallback
            );
        }
    }

    public enum Signal {
        ABORT,
        CONTINUE
    }

    @FunctionalInterface
    public interface CostCallback<T> {

        float apply(T actor, ReadableWorldState worldState);
    }

    @FunctionalInterface
    public interface StartCallback<T> {

        void apply(Context<T> context);
    }

    @FunctionalInterface
    public interface PerformCallback<T> {

        Signal accept(Context<T> context);
    }

    @FunctionalInterface
    public interface FinishCallback<T> {

        void apply(Context<T> context);
    }

    public static final class Context<T> {

        private Action<T> action;

        private T actor;

        private Agent<T> agent;

        private Plan<T> plan;

        private ReadableWorldState worldState;

        private ReadableWorldState previousWorldState;

        private Blackboard blackboard;

        public Context() {}

        public Action<T> action() {
            return action;
        }

        public T actor() {
            return actor;
        }

        public Agent<T> agent() {
            return agent;
        }

        public Plan<T> plan() {
            return plan;
        }

        public ReadableWorldState worldState() {
            return worldState;
        }

        public ReadableWorldState previousWorldState() {
            return previousWorldState;
        }

        public Blackboard blackboard() {
            return blackboard;
        }

        public void set(
            Action<T> action,
            T actor,
            Agent<T> agent,
            Plan<T> plan,
            ReadableWorldState worldState,
            ReadableWorldState previousWorldState,
            Blackboard blackboard
        ) {
            this.action = action;
            this.actor = actor;
            this.agent = agent;
            this.plan = plan;
            this.worldState = worldState;
            this.previousWorldState = previousWorldState;
            this.blackboard = blackboard;
        }
    }
}
