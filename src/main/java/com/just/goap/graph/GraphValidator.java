package com.just.goap.graph;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import com.just.goap.Action;
import com.just.goap.Goal;
import com.just.goap.StateKey;
import com.just.goap.condition.Condition;
import com.just.goap.sensor.Sensor;

class GraphValidator {

    private static final Logger LOGGER = LoggerFactory.getLogger(GraphValidator.class);

    static <T> void validate(
        Set<Action<? super T>> availableActions,
        Set<Goal> availableGoals,
        Map<Condition<?>, Set<Action<? super T>>> preconditionToSatisfyingActionsMap,
        Map<StateKey<?>, ?> retentionPolicyMap,
        Map<StateKey<?>, Sensor<? super T>> sensorMap
    ) {
        var validationErrorCollector = new ValidationErrorCollector();

        // Validate goal preconditions to make sure all precondition keys can be sensed.
        validateGoalPreconditionSatisfiabilityOrThrow(validationErrorCollector, availableGoals, sensorMap);
        // Validate that sensed keys can be sensed from sensors and derived keys can be derived from actions.
        validateKeySatisfiabilityOrThrow(validationErrorCollector, availableActions, sensorMap);
        // Validate unreachable goal conditions.
        validateGoalReachabilityOrThrow(validationErrorCollector, availableGoals, preconditionToSatisfyingActionsMap);
        // Validate dead-end actions.
        validateActionContributionOrThrow(validationErrorCollector, availableActions, availableGoals);
        // Validate retention policies to make sure they are not applied to state keys that are used as preconditions,
        // desired conditions or action effects.
        validateRetentionPolicyUsageOrThrow(
            validationErrorCollector,
            availableActions,
            availableGoals,
            retentionPolicyMap
        );

        validationErrorCollector.flushAndThrowIfAny();
    }

    private static <T> void validateGoalPreconditionSatisfiabilityOrThrow(
        ValidationErrorCollector validationErrorCollector,
        Set<Goal> availableGoals,
        Map<StateKey<?>, Sensor<? super T>> sensorMap
    ) {
        for (var goal : availableGoals) {
            // Check goal preconditions. Preconditions always require a sensor.
            for (var condition : goal.getPreconditions().getConditions()) {
                var sensedKey = condition.key();

                if (!sensorMap.containsKey(sensedKey)) {
                    var quotedSensedKey = "'" + sensedKey + "'";

                    var errorMessage = quotedSensedKey + " is a precondition in '" + goal +
                        "' but " + quotedSensedKey
                        + " has no sensor. Consider adding a sensor that senses " + quotedSensedKey + ".";

                    validationErrorCollector.error(errorMessage);
                }
            }

            // No need to check desired conditions. Desired conditions should always be derived.
        }
    }

    private static <T> void validateKeySatisfiabilityOrThrow(
        ValidationErrorCollector validationErrorCollector,
        Set<Action<? super T>> availableActions,
        Map<StateKey<?>, Sensor<? super T>> sensorMap
    ) {
        // Map each key to its producing actions.
        var derivedKeyProducerMap = new HashMap<StateKey.Derived<?>, Set<Action<? super T>>>();

        for (var action : availableActions) {
            action.getEffectContainer().getEffects().forEach(effect -> {
                derivedKeyProducerMap
                    .computeIfAbsent(effect.key(), $ -> new HashSet<>())
                    .add(action);
            });
        }

        for (var action : availableActions) {
            for (var precondition : action.getPreconditionContainer().getConditions()) {
                var key = precondition.key();

                switch (key) {
                    case StateKey.Derived<?> derivedKey -> {
                        var keyProducers = derivedKeyProducerMap.getOrDefault(derivedKey, Set.of());

                        // If this key is only produced by THIS action and no sensor exists, error.
                        var onlySelfProduces = keyProducers.size() == 1 && keyProducers.contains(action);
                        var noProducers = keyProducers.isEmpty();
                        var noSensor = !sensorMap.containsKey(derivedKey);

                        if ((onlySelfProduces || noProducers) && noSensor) {
                            var quotedPrecondition = "'" + precondition + "'";
                            var quotedAction = "'" + action + "'";
                            var errorMessage = String.format(
                                """
                                    Action %s has a precondition %s with no possible action that can satisfy %s. Possible fixes:
                                        - Add an action that can satisfy the precondition %s
                                        - Remove the precondition %s from %s""",
                                quotedAction,
                                quotedPrecondition,
                                quotedPrecondition,
                                quotedPrecondition,
                                quotedPrecondition,
                                quotedAction
                            );

                            validationErrorCollector.error(errorMessage);
                        }
                    }
                    case StateKey.Sensed<?> sensedKey -> {
                        var noSensor = !sensorMap.containsKey(sensedKey);

                        if (noSensor) {
                            var quotedPrecondition = "'" + precondition + "'";
                            var quotedPreconditionKey = "'" + precondition.key().id() + "'";
                            var quotedAction = "'" + action + "'";
                            var errorMessage = String.format(
                                """
                                    Action %s has a precondition %s with no possible sensor that can sense %s. Possible fixes:
                                        - Add a sensor that senses %s
                                        - Remove the precondition %s from %s""",
                                quotedAction,
                                quotedPrecondition,
                                quotedPreconditionKey,
                                quotedPreconditionKey,
                                quotedPrecondition,
                                quotedAction
                            );

                            validationErrorCollector.error(errorMessage);
                        }
                    }
                }
            }
        }
    }

    private static <T> void validateGoalReachabilityOrThrow(
        ValidationErrorCollector validationErrorCollector,
        Set<Goal> availableGoals,
        Map<Condition<?>, Set<Action<? super T>>> preconditionToSatisfyingActionsMap
    ) {
        for (var goal : availableGoals) {
            for (var desiredCondition : goal.getDesiredConditions().getConditions()) {
                var satisfyingActions = preconditionToSatisfyingActionsMap.getOrDefault(desiredCondition, Set.of());

                if (satisfyingActions.isEmpty()) {
                    var quotedGoal = "'" + goal + "'";
                    var quotedPrecondition = "'" + desiredCondition + "'";
                    var errorMessage = String.format(
                        """
                            Goal %s has a desired condition %s with no possible action that can satisfy it. Possible fixes:
                                - Add an action that can satisfy the desired precondition %s
                                - Remove the desired precondition %s from %s""",
                        quotedGoal,
                        quotedPrecondition,
                        quotedPrecondition,
                        quotedPrecondition,
                        quotedGoal
                    );

                    validationErrorCollector.error(errorMessage);
                }
            }
        }
    }

    private static <T> void validateActionContributionOrThrow(
        ValidationErrorCollector validationErrorCollector,
        Set<Action<? super T>> availableActions,
        Set<Goal> availableGoals
    ) {
        var usefulConditions = new HashSet<Condition<?>>();
        var reachableActions = new HashSet<Action<? super T>>();

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
                var quotedAction = "'" + action + "'";
                var errorMessage = String.format("""
                    Action %s is a dead end and doesn't satisfy any other action or goal. Possible fixes:
                        - Change the effects of %s such that one of the effects satisfies an action or goal precondition
                        - Change the desired conditions of one of your goals to match the effects of %s
                        - Remove %s""", quotedAction, quotedAction, quotedAction, quotedAction);

                validationErrorCollector.error(errorMessage);
            }
        }
    }

    private static <T> void validateRetentionPolicyUsageOrThrow(
        ValidationErrorCollector validationErrorCollector,
        Set<Action<? super T>> availableActions,
        Set<Goal> availableGoals,
        Map<StateKey<?>, ?> retentionPolicyMap
    ) {
        // Map of key -> usages (for diagnostics)
        record Usage(
            String type,
            String owner
        ) {}

        var keyUsages = new HashMap<StateKey<?>, Set<Usage>>();

        // 1. Collect from actions
        for (var action : availableActions) {
            var actionName = action.toString();

            // Preconditions
            for (var pre : action.getPreconditionContainer().getConditions()) {
                keyUsages.computeIfAbsent(pre.key(), $ -> new HashSet<>())
                    .add(new Usage("Action precondition", actionName));
            }

            // Effects
            action.getEffectContainer()
                .getEffects()
                .forEach(
                    effect -> keyUsages.computeIfAbsent(effect.key(), $ -> new HashSet<>())
                        .add(new Usage("Action effect", actionName))
                );
        }

        // 2. Collect from goals
        for (var goal : availableGoals) {
            var goalName = goal.toString();

            // Preconditions
            goal.getPreconditions()
                .getConditions()
                .forEach(
                    cond -> keyUsages.computeIfAbsent(cond.key(), $ -> new HashSet<>())
                        .add(new Usage("Goal precondition", goalName))
                );

            // Desired conditions
            goal.getDesiredConditions()
                .getConditions()
                .forEach(
                    cond -> keyUsages.computeIfAbsent(cond.key(), $ -> new HashSet<>())
                        .add(new Usage("Goal desired condition", goalName))
                );
        }

        // 3. Now check for retention policy overlap
        for (var entry : keyUsages.entrySet()) {
            var key = entry.getKey();

            if (!retentionPolicyMap.containsKey(key)) {
                continue;
            }

            var usages = entry.getValue();

            // Build a human-readable description of all usages.
            var usageDetails = usages.stream()
                .map(u -> String.format("- %s in '%s'", u.type(), u.owner()))
                .sorted()
                .toList();

            var joinedUsageDetails = String.join("\n\t", usageDetails);

            var errorMessage = String.format(
                """
                    Retention policy found for key '%s', which is used in one or more planner-critical contexts:
                        %s
                        Possible fixes:
                            - Remove the retention policy for '%s'
                            - Modify the listed actions or goals to avoid using '%s' in those contexts
                    """,
                key,
                joinedUsageDetails,
                key,
                key
            );

            validationErrorCollector.error(errorMessage);
        }
    }

    private GraphValidator() {
        throw new UnsupportedOperationException();
    }

    static class ValidationErrorCollector {

        private final Set<String> errors = new LinkedHashSet<>();

        void error(String message) {
            errors.add(message);
        }

        void flushAndThrowIfAny() {
            if (!errors.isEmpty()) {
                LOGGER.error("GOAP graph validation failed with the following errors:");

                for (var error : errors) {
                    LOGGER.error("- {}", error);
                }

                System.err.println("GOAP graph validation failed with the following errors:");

                for (var error : errors) {
                    System.err.println("- " + error);
                }

                throw new IllegalStateException("GOAP graph validation failed.");
            }
        }
    }

}
