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

        if (currentPlan == null) {
            this.currentPlan = PlanFactory.create(graph, context, worldState);
        }

        if (currentPlan != null) {
            var planState = currentPlan.update(context, worldState);

            switch (planState) {
                case Plan.State.Failed ignored -> this.currentPlan = null;
                case Plan.State.Finished ignored -> this.currentPlan = null;
                case Plan.State.Invalid ignored -> this.currentPlan = null;
                case Plan.State.InProgress ignored -> {/* NO-OP */}
            }
        }
    }
}
