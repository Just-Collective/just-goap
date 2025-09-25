package com.just.goap;

import com.just.goap.graph.Graph;
import com.just.goap.plan.Plan;
import com.just.goap.plan.PlanFactory;
import com.just.goap.state.MutableWorldState;
import com.just.goap.state.WorldState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class GOAP<T> {

    private final Graph<T> graph;

    private final List<Sensor<? super T>> sensors;

    private @Nullable Plan<T> currentPlan;

    private boolean isEnabled;

    protected GOAP(Graph<T> graph) {
        this.graph = graph;
        this.sensors = new ArrayList<>();
        this.currentPlan = null;
        this.isEnabled = true;
    }

    public void addSensor(Sensor<? super T> sensor) {
        sensors.add(sensor);
    }

    public WorldState sense(T context) {
        var state = new MutableWorldState();

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
        isEnabled = enabled;
    }

    public Graph<T> getGraph() {
        return graph;
    }
}
