package com.just.goap.graph;

import com.just.goap.Action;
import com.just.goap.GOAPKey;
import com.just.goap.Goal;
import com.just.goap.Sensor;
import com.just.goap.condition.Condition;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

public class Graph<T> {

    public static <T> Builder<T> builder() {
        return new Builder<>();
    }

    private final Set<Action<T>> availableActions;

    private final Set<Goal> availableGoals;

    private final Map<Condition<?>, Set<Action<T>>> preconditionToSatisfyingActionsMap;

    private final Map<GOAPKey<?>, Sensor<T, ?>> sensorMap;

    private Graph(
        Set<Action<T>> availableActions,
        Set<Goal> availableGoals,
        Map<Condition<?>, Set<Action<T>>> preconditionToSatisfyingActionsMap,
        Map<GOAPKey<?>, Sensor<T, ?>> sensorMap
    ) {
        this.availableActions = availableActions;
        this.availableGoals = availableGoals;
        this.preconditionToSatisfyingActionsMap = preconditionToSatisfyingActionsMap;
        this.sensorMap = sensorMap;
    }

    public Set<Action<T>> getAvailableActions() {
        return availableActions;
    }

    public Set<Goal> getAvailableGoals() {
        return availableGoals;
    }

    public Set<Action<T>> getActionsThatSatisfy(Condition<?> condition) {
        return preconditionToSatisfyingActionsMap.getOrDefault(condition, Set.of());
    }

    public Map<GOAPKey<?>, Sensor<T, ?>> getSensorMap() {
        return sensorMap;
    }

    public static class Builder<T> {

        private final Set<Action<T>> availableActions;

        private final Set<Goal> availableGoals;

        private final Map<Condition<?>, Set<Action<T>>> preconditionToSatisfyingActionsMap;

        private final Map<GOAPKey<?>, Sensor<T, ?>> sensorMap;

        private Builder() {
            this.availableActions = new HashSet<>();
            this.availableGoals = new HashSet<>();
            this.preconditionToSatisfyingActionsMap = new HashMap<>();
            this.sensorMap = new HashMap<>();
        }

        public <U> Builder<T> addSensor(GOAPKey<U> key, Function<T, U> extractor) {
            return addSensor(Sensor.direct(key, extractor));
        }

        public <U, V> Builder<T> addSensor(
            GOAPKey<U> key,
            GOAPKey<V> sourceKey,
            BiFunction<T, V, U> extractor
        ) {
            return addSensor(Sensor.derived(key, sourceKey, extractor));
        }

        public <U> Builder<T> addSensor(Sensor<T, ? super U> sensor) {
            sensorMap.put(sensor.key(), sensor);
            return this;
        }

        public Builder<T> addGoal(Goal goal) {
            var desiredConditions = goal.getDesiredConditions().getConditions();

            if (desiredConditions.isEmpty()) {
                throw new IllegalArgumentException("Goal must specify at least one desired condition: " + goal);
            }

            availableGoals.add(goal);

            for (var condition : desiredConditions) {
                // Ensure condition is in map.
                var actions = preconditionToSatisfyingActionsMap.computeIfAbsent(condition, $ -> new HashSet<>());

                // Check if any existing action satisfies it.
                for (var action : availableActions) {
                    if (condition.satisfiedBy(action.getEffectContainer())) {
                        actions.add(action);
                    }
                }
            }

            return this;
        }

        public Builder<T> addAction(Action<T> action) {
            if (action.getEffectContainer().getEffects().isEmpty()) {
                throw new IllegalArgumentException("Action must have at least one effect: " + action);
            }

            availableActions.add(action);

            var effects = action.getEffectContainer();
            // Collect all newly added preconditions.
            var newPreconditions = new HashSet<Condition<?>>();

            for (var precondition : action.getPreconditionContainer().getConditions()) {
                preconditionToSatisfyingActionsMap.computeIfAbsent(precondition, $ -> {
                    // Track brand new condition.
                    newPreconditions.add(precondition);
                    return new HashSet<>();
                });
            }

            // For each *existing* condition, check if the new action satisfies it.
            for (var condition : preconditionToSatisfyingActionsMap.keySet()) {
                if (condition.satisfiedBy(effects)) {
                    var actions = preconditionToSatisfyingActionsMap.get(condition);
                    actions.add(action);
                }
            }

            // For each *newly added condition*, check if any *existing* actions satisfy it.
            for (var newPrecondition : newPreconditions) {
                var actions = preconditionToSatisfyingActionsMap.get(newPrecondition);

                for (var otherAction : availableActions) {
                    if (otherAction == action) {
                        continue;
                    }

                    if (newPrecondition.satisfiedBy(otherAction.getEffectContainer())) {
                        actions.add(otherAction);
                    }
                }
            }

            return this;
        }

        public Graph<T> build() {
            GraphValidator.validate(
                availableActions,
                availableGoals,
                preconditionToSatisfyingActionsMap,
                sensorMap
            );

            return new Graph<>(
                Collections.unmodifiableSet(availableActions),
                Collections.unmodifiableSet(availableGoals),
                Collections.unmodifiableMap(preconditionToSatisfyingActionsMap),
                Collections.unmodifiableMap(sensorMap)
            );
        }

    }
}
