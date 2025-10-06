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

        if (!hasPlan()) {
            this.currentPlan = PlanFactory.create(graph, context, worldState);
        }

        if (hasPlan()) {
            var planState = currentPlan.update(context, worldState);

            switch (planState) {
                case FAILED, FINISHED, INVALID -> this.currentPlan = null;
                case IN_PROGRESS -> {/* NO-OP */}
            }
        }
    }

    public boolean hasPlan() {
        return currentPlan != null;
    }
}
