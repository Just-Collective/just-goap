package com.just.goap;

import com.just.goap.graph.GOAPGraph;
import com.just.goap.plan.GOAPPlan;
import com.just.goap.plan.GOAPPlanner;
import com.just.goap.state.GOAPMutableWorldState;
import com.just.goap.state.GOAPWorldState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class GOAP<T> {

    private final GOAPGraph<T> graph;

    private final List<GOAPSensor<? super T>> sensors;

    private @Nullable GOAPPlan<T> currentPlan;

    private boolean isEnabled;

    protected GOAP(GOAPGraph<T> graph) {
        this.graph = graph;
        this.sensors = new ArrayList<>();
        this.currentPlan = null;
        this.isEnabled = true;
    }

    public void addSensor(GOAPSensor<? super T> sensor) {
        sensors.add(sensor);
    }

    public GOAPWorldState sense(T context) {
        var state = new GOAPMutableWorldState();

        for (var sensor : sensors) {
            sensor.sense(context, state);
        }

        return state;
    }

    public void update(T context) {
        if (!isEnabled) {
            return;
        }

        // Sense all world input that we need to.
        var worldState = sense(context);

        if (currentPlan == null) {
            this.currentPlan = GOAPPlanner.createPlan(graph, context, worldState);
        }

        if (currentPlan != null) {
            var planState = currentPlan.update(context, worldState);

            switch (planState) {
                case GOAPPlan.State.Failed ignored -> this.currentPlan = null;
                case GOAPPlan.State.Finished ignored -> this.currentPlan = null;
                case GOAPPlan.State.Invalid ignored -> this.currentPlan = null;
                case GOAPPlan.State.InProgress ignored -> {/* NO-OP */}
            }
        }
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }

    public GOAPGraph<T> getGraph() {
        return graph;
    }
}
