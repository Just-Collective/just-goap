package com.just.goap;

import com.just.goap.graph.Graph;
import com.just.goap.plan.Plan;
import com.just.goap.plan.PlanFactory;
import com.just.goap.state.SensingMutableWorldState;
import org.jetbrains.annotations.Nullable;

public final class GOAP<T> {

    public static <T> GOAP<T> of(Graph<T> graph) {
        return new GOAP<>(graph);
    }

    private final Graph<T> graph;

    private @Nullable Plan<T> currentPlan;

    private boolean isEnabled;

    private GOAP(Graph<T> graph) {
        this.graph = graph;
        this.currentPlan = null;
        this.isEnabled = true;
    }

    public void update(T context) {
        if (!isEnabled) {
            return;
        }

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

    public void setEnabled(boolean enabled) {
        this.isEnabled = enabled;
    }

    public Graph<T> getGraph() {
        return graph;
    }
}
