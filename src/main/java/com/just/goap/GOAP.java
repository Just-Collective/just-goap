package com.just.goap;

import com.just.goap.graph.Graph;
import com.just.goap.plan.Plan;
import com.just.goap.plan.PlanFactory;
import com.just.goap.state.SensingMutableWorldState;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

public final class GOAP<T> {

    public static <T> Builder<T> builder() {
        return new Builder<>();
    }

    private final Graph<T> graph;

    private final Map<TypedIdentifier<?>, Sensor<T, ?>> sensorsByIdentifierMap;

    private @Nullable Plan<T> currentPlan;

    private boolean isEnabled;

    private GOAP(Graph<T> graph, Map<TypedIdentifier<?>, Sensor<T, ?>> sensorsByIdentifierMap) {
        this.graph = graph;
        this.sensorsByIdentifierMap = sensorsByIdentifierMap;
        this.currentPlan = null;
        this.isEnabled = true;
    }

    public void update(T context) {
        if (!isEnabled) {
            return;
        }

        var worldState = new SensingMutableWorldState<>(context, sensorsByIdentifierMap);

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

    public static class Builder<T> {

        private final Graph.Builder<T> graphBuilder;

        private final Map<TypedIdentifier<?>, Sensor<T, ?>> sensorsByIdentifierMap;

        private Builder() {
            this.graphBuilder = Graph.builder();
            this.sensorsByIdentifierMap = new HashMap<>();
        }

        public <U> Builder<T> addSensor(TypedIdentifier<U> identifier, Function<T, U> extractor) {
            return addSensor(Sensor.direct(identifier, extractor));
        }

        public <U, V> Builder<T> addSensor(
            TypedIdentifier<U> identifier,
            TypedIdentifier<V> sourceIdentifier,
            BiFunction<T, V, U> extractor
        ) {
            return addSensor(Sensor.derived(identifier, sourceIdentifier, extractor));
        }

        public <U> Builder<T> addSensor(Sensor<T, ? super U> sensor) {
            sensorsByIdentifierMap.put(sensor.identifier(), sensor);
            return this;
        }

        public Builder<T> addGoal(Goal goal) {
            graphBuilder.addGoal(goal);
            return this;
        }

        public Builder<T> addAction(Action<T> action) {
            graphBuilder.addAction(action);
            return this;
        }

        public GOAP<T> build() {
            return new GOAP<>(graphBuilder.build(), Collections.unmodifiableMap(sensorsByIdentifierMap));
        }
    }
}
