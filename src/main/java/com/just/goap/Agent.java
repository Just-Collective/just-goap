package com.just.goap;

import com.just.goap.graph.Graph;
import com.just.goap.plan.DefaultPlanFactory;
import com.just.goap.plan.Plan;
import com.just.goap.state.SensingMutableWorldState;
import org.jetbrains.annotations.Nullable;

public final class Agent<T> {

    public static <T> Agent<T> create() {
        return Agent.<T>builder().build();
    }

    public static <T> Builder<T> builder() {
        return new Builder<>();
    }

    private final PlanFactory<T> planFactory;

    private @Nullable Plan<T> currentPlan;

    private Agent(PlanFactory<T> planFactory) {
        this.planFactory = planFactory;
        this.currentPlan = null;
    }

    public void update(Graph<T> graph, T context) {
        var worldState = new SensingMutableWorldState<>(context, graph.getSensorMap());
        // Local capture of current plan just in case agent plan is abandoned from another thread.
        var currentPlan = this.currentPlan;

        if (currentPlan == null) {
            currentPlan = planFactory.create(graph, context, worldState);
        }

        if (currentPlan != null) {
            var planState = currentPlan.update(context, worldState);

            switch (planState) {
                case FAILED, FINISHED, INVALID -> currentPlan = null;
                case IN_PROGRESS -> {/* NO-OP */}
            }
        }

        // Assign current plan to whatever the plan factory came up with (if anything).
        this.currentPlan = currentPlan;
    }

    public void abandonPlan() {
        this.currentPlan = null;
    }

    public boolean hasPlan() {
        return currentPlan != null;
    }

    public interface PlanFactory<T> {

        @Nullable
        Plan<T> create(Graph<T> graph, T context, SensingMutableWorldState<T> worldState);
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
