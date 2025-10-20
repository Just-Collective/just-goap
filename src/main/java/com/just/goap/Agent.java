package com.just.goap;

import org.jetbrains.annotations.Nullable;

import com.just.goap.graph.Graph;
import com.just.goap.plan.DefaultPlanFactory;
import com.just.goap.plan.Plan;
import com.just.goap.state.SensingWorldState;
import com.just.goap.state.WorldState;

public final class Agent<T> {

    public static <T> Agent<T> create() {
        return Agent.<T>builder().build();
    }

    public static <T> Builder<T> builder() {
        return new Builder<>();
    }

    private final PlanFactory<T> planFactory;

    private final WorldState previousWorldState;

    private final SensingWorldState<T> currentWorldState;

    private @Nullable Plan<T> currentPlan;


    private long tick;

    private Agent(PlanFactory<T> planFactory) {
        this.planFactory = planFactory;
        this.currentPlan = null;
        this.previousWorldState = WorldState.create();
        this.currentWorldState = new SensingWorldState<>();
        this.tick = 0;
    }

    public void update(Graph<T> graph, T context) {
        prepareWorldStates(graph, context);
        createPlan(graph, context);
        updatePlan(context);
        tick++;
    }

    public void abandonPlan() {
        this.currentPlan = null;
    }

    public boolean hasPlan() {
        return currentPlan != null;
    }

    public long getTick() {
        return tick;
    }

    private void prepareWorldStates(Graph<T> graph, T context) {
        currentWorldState.setSensorMap(graph.getSensorMap());
        currentWorldState.setContext(context);

        // Clear the previous world state.
        previousWorldState.clear();
        // Set the previous world state's contents to the current world state's contents.
        previousWorldState.setAll(currentWorldState.getMap());
        // Clear current world state before we use it.
        currentWorldState.clear();
    }

    private void createPlan(Graph<T> graph, T context) {
        if (currentPlan == null) {
            this.currentPlan = planFactory.create(graph, context, currentWorldState);
        }
    }

    private void updatePlan(T context) {
        if (currentPlan != null) {
            var planState = currentPlan.update(context, currentWorldState);

            switch (planState) {
                case ABORTED, FINISHED, INVALID -> this.currentPlan = null;
                case IN_PROGRESS -> {/* NO-OP */}
            }
        }
    }

    public interface PlanFactory<T> {

        @Nullable
        Plan<T> create(Graph<T> graph, T context, SensingWorldState<T> worldState);
    }

    public static class Builder<T> {

        private PlanFactory<T> planFactory;

        private Builder() {
            this.planFactory = DefaultPlanFactory::create;
        }

        public Builder<T> withPlanFactory(PlanFactory<T> planFactory) {
            this.planFactory = planFactory;
            return this;
        }

        public Agent<T> build() {
            return new Agent<>(planFactory);
        }
    }
}
