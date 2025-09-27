package com.just.goap.graph;

import com.just.goap.Action;
import com.just.goap.Goal;
import com.just.goap.Sensor;
import com.just.goap.TypedIdentifier;
import com.just.goap.condition.Condition;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

class GraphValidator {

    static <T> void validate(
        Set<Action<T>> availableActions,
        Set<Goal> availableGoals,
        Map<Condition<?>, Set<Action<T>>> preconditionToSatisfyingActionsMap,
        Map<TypedIdentifier<?>, Sensor<T, ?>> sensorsByIdentifierMap
    ) {
        // Validate sensor coverage to make sure all typed identifiers are observable.
        validateSensorCoverageOrThrow(availableActions, availableGoals, sensorsByIdentifierMap);
        // Validate unreachable goal conditions.
        validateGoalReachabilityOrThrow(availableGoals, preconditionToSatisfyingActionsMap);
        // Validate dead-end actions.
        validateActionContributionOrThrow(availableActions, availableGoals);
    }

    private static <T> void validateSensorCoverageOrThrow(
        Set<Action<T>> availableActions,
        Set<Goal> availableGoals,
        Map<TypedIdentifier<?>, Sensor<T, ?>> sensorsByIdentifierMap
    ) {
        var missing = new HashSet<TypedIdentifier<?>>();

        // Check goal desired conditions
        for (var goal : availableGoals) {
            for (var condition : goal.getDesiredConditions().getConditions()) {
                var identifier = condition.identifier();

                if (!sensorsByIdentifierMap.containsKey(identifier)) {
                    missing.add(identifier);
                }
            }
        }

        // Check action preconditions
        for (var action : availableActions) {
            for (var condition : action.getPreconditionContainer().getConditions()) {
                var identifier = condition.identifier();

                if (!sensorsByIdentifierMap.containsKey(identifier)) {
                    missing.add(identifier);
                }
            }
        }

        if (!missing.isEmpty()) {
            throw new IllegalStateException("Missing sensors for identifiers: " + missing);
        }
    }

    private static <T> void validateActionContributionOrThrow(
        Set<Action<T>> availableActions,
        Set<Goal> availableGoals
    ) {
        var usefulConditions = new HashSet<Condition<?>>();
        var reachableActions = new HashSet<Action<T>>();

        // Start from goal desired conditions.
        for (var goal : availableGoals) {
            usefulConditions.addAll(goal.getDesiredConditions().getConditions());
        }

        // Propagate backwards to find all useful conditions/actions.
        boolean changed;

        do {
            changed = false;

            for (var action : availableActions) {
                if (reachableActions.contains(action)) {
                    continue;
                }

                // If action effects satisfy any useful condition
                var useful = usefulConditions.stream()
                    .anyMatch(condition -> condition.satisfiedBy(action.getEffectContainer()));

                if (useful) {
                    reachableActions.add(action);

                    for (var pre : action.getPreconditionContainer().getConditions()) {
                        if (usefulConditions.add(pre)) {
                            changed = true;
                        }
                    }
                    changed = true;
                }
            }
        } while (changed);

        for (var action : availableActions) {
            if (!reachableActions.contains(action)) {
                throw new IllegalStateException(
                    "Dead-end action: " + action + " has no contribution to any goal or precondition."
                );
            }
        }
    }

    private static <T> void validateGoalReachabilityOrThrow(
        Set<Goal> availableGoals,
        Map<Condition<?>, Set<Action<T>>> preconditionToSatisfyingActionsMap
    ) {
        for (var goal : availableGoals) {
            for (var desiredCondition : goal.getDesiredConditions().getConditions()) {
                var satisfyingActions = preconditionToSatisfyingActionsMap.getOrDefault(desiredCondition, Set.of());

                if (satisfyingActions.isEmpty()) {
                    throw new IllegalStateException(
                        "No action satisfies goal condition: " + desiredCondition + " in goal: " + goal
                    );
                }
            }
        }
    }

    private GraphValidator() {
        throw new UnsupportedOperationException();
    }
}
