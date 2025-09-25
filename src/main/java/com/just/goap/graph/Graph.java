package com.just.goap.graph;

import com.just.goap.Action;
import com.just.goap.Goal;
import com.just.goap.condition.Condition;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Graph<T> {

    public static <T> Builder<T> builder() {
        return new Builder<>();
    }

    private final Set<Action<T>> availableActions;

    private final Set<Goal> availableGoals;

    private final Map<Condition<?>, Set<Action<T>>> preconditionToSatisfyingActionsMap;

    private Graph(
        Set<Action<T>> availableActions,
        Set<Goal> availableGoals,
        Map<Condition<?>, Set<Action<T>>> preconditionToSatisfyingActionsMap
    ) {
        this.availableActions = availableActions;
        this.availableGoals = availableGoals;
        this.preconditionToSatisfyingActionsMap = preconditionToSatisfyingActionsMap;
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

    public static class Builder<T> {

        private final Set<Action<T>> availableActions;

        private final Set<Goal> availableGoals;

        private final Map<Condition<?>, Set<Action<T>>> preconditionToSatisfyingActionsMap;

        private Builder() {
            this.availableActions = new HashSet<>();
            this.availableGoals = new HashSet<>();
            this.preconditionToSatisfyingActionsMap = new HashMap<>();
        }

        public Builder<T> addGoal(Goal goal) {
            availableGoals.add(goal);

            var newConditions = goal.getDesiredConditions().getConditions();

            for (var condition : newConditions) {
                // Ensure condition is in map.
                var actions = preconditionToSatisfyingActionsMap.computeIfAbsent(condition, $ -> new HashSet<>());

                // Check if any existing action satisfies it.
                for (var action : availableActions) {
                    if (condition.satisfiedBy(action.getEffects())) {
                        actions.add(action);
                    }
                }
            }

            return this;
        }

        public Builder<T> addAction(Action<T> action) {
            availableActions.add(action);

            var effects = action.getEffects();
            // Collect all newly added preconditions.
            var newPreconditions = new HashSet<Condition<?>>();

            for (var precondition : action.getPreconditions().getConditions()) {
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

                    if (newPrecondition.satisfiedBy(otherAction.getEffects())) {
                        actions.add(otherAction);
                    }
                }
            }

            return this;
        }

        public Graph<T> build() {
            return new Graph<>(
                Collections.unmodifiableSet(availableActions),
                Collections.unmodifiableSet(availableGoals),
                Collections.unmodifiableMap(preconditionToSatisfyingActionsMap)
            );
        }
    }
}
