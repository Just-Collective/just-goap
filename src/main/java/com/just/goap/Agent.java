package com.just.goap;

import com.just.goap.graph.Graph;
import com.just.goap.plan.Plan;
import com.just.goap.plan.PlanFactory;
import com.just.goap.state.SensingMutableWorldState;
import org.jetbrains.annotations.Nullable;

public final class Agent<T> {

    public static <T> Agent<T> create() {
        return new Agent<>();
    }

    private @Nullable Plan<T> currentPlan;

    private Agent() {
        this.currentPlan = null;
    }

    public void update(Graph<T> graph, T context) {
        var worldState = new SensingMutableWorldState<>(context, graph.getSensorMap());
        // Local capture of current plan just in case agent plan is abandoned from another thread.
        var currentPlan = this.currentPlan;

        if (currentPlan == null) {
            currentPlan = PlanFactory.create(graph, context, worldState);
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
}
