package com.just.goap.action;

import com.just.goap.Agent;
import com.just.goap.condition.ConditionContainer;
import com.just.goap.effect.EffectContainer;
import com.just.goap.plan.Plan;
import com.just.goap.state.Blackboard;
import com.just.goap.state.ReadableWorldState;

public interface Action<T> {

    static <T> BaseAction.ConcreteBuilder<T> builder(String name) {
        return BaseAction.builder(name);
    }

    float getCost(T actor, ReadableWorldState worldState);

    Signal perform(Context<? extends T> context);

    void onStart(Context<? extends T> context);

    void onFinish(Context<? extends T> context);

    ConditionContainer getPreconditionContainer();

    EffectContainer getEffectContainer();

    String getName();

    enum Signal {
        ABORT,
        CONTINUE
    }

    @FunctionalInterface
    interface CostCallback<T> {

        float apply(T actor, ReadableWorldState worldState);
    }

    @FunctionalInterface
    interface StartCallback<T> {

        void apply(Context<? extends T> context);
    }

    @FunctionalInterface
    interface PerformCallback<T> {

        Signal accept(Context<? extends T> context);
    }

    @FunctionalInterface
    interface FinishCallback<T> {

        void apply(Context<? extends T> context);
    }

    final class Context<T> {

        private Action<T> action;

        private T actor;

        private Agent<T> agent;

        private Blackboard blackboard;

        private Plan<T> plan;

        private ReadableWorldState worldState;

        private ReadableWorldState previousWorldState;

        public Context() {}

        public Action<T> getAction() {
            return action;
        }

        public T getActor() {
            return actor;
        }

        public Agent<T> getAgent() {
            return agent;
        }

        public Blackboard getBlackboard(Blackboard.Scope scope) {
            return switch (scope) {
                case ACTION -> blackboard;
                case AGENT -> agent.getBlackboard();
                case GRAPH -> agent.getGraphBlackboard();
                case PLAN -> plan.getBlackboard();
            };
        }

        public Plan<T> getPlan() {
            return plan;
        }

        public ReadableWorldState getWorldState() {
            return worldState;
        }

        public ReadableWorldState getPreviousWorldState() {
            return previousWorldState;
        }

        public void set(
            Action<T> action,
            T actor,
            Agent<T> agent,
            Blackboard blackboard,
            Plan<T> plan,
            ReadableWorldState worldState,
            ReadableWorldState previousWorldState
        ) {
            this.action = action;
            this.actor = actor;
            this.agent = agent;
            this.blackboard = blackboard;
            this.plan = plan;
            this.worldState = worldState;
            this.previousWorldState = previousWorldState;
        }
    }
}
