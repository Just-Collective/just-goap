package com.just.goap.plan;

import com.just.goap.AOStar;
import com.just.goap.graph.Graph;
import com.just.goap.state.SensingMutableWorldState;
import org.jetbrains.annotations.Nullable;

public class DefaultPlanFactory {

    public static <T> @Nullable Plan<T> create(
        Graph<T> graph,
        T context,
        SensingMutableWorldState<T> initialWorldState
    ) {
        Plan<T> bestPlan = null;
        float bestCost = Float.MAX_VALUE;

        for (var goal : graph.getAvailableGoals()) {
            if (!goal.getPreconditions().satisfiedBy(initialWorldState)) {
                // Goal's preconditions are not satisfied by the current world state, skip the goal.
                continue;
            }

            // We need to find actions that satisfy these conditions.
            var desiredConditions = goal.getDesiredConditions();

            var plan = AOStar.solve(graph, desiredConditions, initialWorldState, context);

            if (plan != null && !plan.isEmpty()) {
                // FIXME: The cost-per-action here is evaluated using the wrong world state. We need to use the
                // simulated state.
                var cost = plan.stream()
                    .map(action -> action.getCost(context, initialWorldState))
                    .reduce(0.0f, Float::sum);

                if (cost < bestCost) {
                    bestCost = cost;
                    bestPlan = new Plan<>(goal, plan);
                }
            }
        }

        return bestPlan;
    }

    private DefaultPlanFactory() {
        throw new UnsupportedOperationException();
    }
}
