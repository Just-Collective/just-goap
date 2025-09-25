package com.just.goap.plan;

import com.just.goap.Action;
import com.just.goap.condition.ConditionContainer;
import com.just.goap.graph.Graph;
import com.just.goap.state.MutableWorldState;
import com.just.goap.state.WorldState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class PlanFactory {

    public static <T> @Nullable Plan<T> create(Graph<T> graph, T context, WorldState currentState) {
        Plan<T> bestPlan = null;
        float bestCost = Float.MAX_VALUE;

        for (var goal : graph.getAvailableGoals()) {
            // We need to find actions that satisfy these conditions.
            var desiredConditions = goal.getDesiredConditions();

            var plan = buildPlanForConditions(graph, desiredConditions, currentState);

            if (plan != null && !plan.isEmpty()) {
                // FIXME: The cost-per-action here is evaluated using the wrong world state. We need to use the
                // simulated state.
                var cost = plan.stream()
                    .map(action -> action.getCost(context, currentState))
                    .reduce(0.0f, Float::sum);

                if (cost < bestCost) {
                    bestCost = cost;
                    bestPlan = new Plan<>(goal, plan);
                }
            }
        }

        return bestPlan;
    }

    private static <T> @Nullable List<Action<T>> buildPlanForConditions(
        Graph<T> graph,
        ConditionContainer desiredConditions,
        WorldState currentState
    ) {
        if (currentState.satisfies(desiredConditions)) {
            // The current world state already satisfies the desired conditions, there are no further actions necessary
            // to achieve the desired conditions.
            return List.of();
        }

        // Prepare a new plan (sequential list of actions).
        var plan = new ArrayList<Action<T>>();
        // Create a working world state. We will use this to "simulate" the effects of actions on the world state.
        var workingState = new MutableWorldState(currentState);

        // For every desired condition, we need to figure out if the condition is already satisfied.
        for (var condition : desiredConditions.getConditions()) {
            if (condition.satisfiedBy(workingState)) {
                // The condition is already satisfied, no further actions necessary to try and satisfy it.
                continue;
            }

            // The condition is not satisfied by the working world state, get actions that can satisfy the condition.
            var satisfyingActions = graph.getActionsThatSatisfy(condition);

            var satisfied = false;

            // For the given desired condition, we may have multiple actions that can potentially satisfy the desired
            // condition. So we need to go through all of them and see which actions are actually viable, if any.
            // FIXME: This code will pick the first action with preconditions that can be satisfied.
            // FIXME: As a consequence, there is no evaluation of which action has a lower cost.
            for (var action : satisfyingActions) {
                // Build a subplan for the current action. Every action has preconditions, and like our original
                // desired conditions, we need to make sure we have a viable plan to solve those preconditions.
                var subPlan = buildPlanForConditions(graph, action.getPreconditions(), workingState);

                if (subPlan != null) {
                    // If sub plan is valid, it will be non-null. We can add all of its actions to our current plan.
                    plan.addAll(subPlan);
                    // The plan to arrive at our current action was valid, so the action itself is also valid. Add it.
                    plan.add(action);
                    // Update our working state with the action's effects to simulate the action being done.
                    workingState.apply(action.getEffects());
                    satisfied = true;
                    break;
                }
            }

            if (!satisfied) {
                // There was no action that could satisfy the current condition, so return null.
                return null;
            }
        }

        return plan;
    }

    private PlanFactory() {
        throw new UnsupportedOperationException();
    }
}
