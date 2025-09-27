package com.just.goap.plan;

import com.just.goap.AOStar;
import com.just.goap.graph.Graph;
import com.just.goap.state.SensingMutableWorldState;
import org.jetbrains.annotations.Nullable;

public class PlanFactory {

    public static <T> @Nullable Plan<T> create(Graph<T> graph, T context, SensingMutableWorldState<T> currentState) {
        Plan<T> bestPlan = null;
        float bestCost = Float.MAX_VALUE;

        for (var goal : graph.getAvailableGoals()) {
            // We need to find actions that satisfy these conditions.
            var desiredConditions = goal.getDesiredConditions();

            var plan = AOStar.solve(graph, desiredConditions, currentState, context);

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

    private PlanFactory() {
        throw new UnsupportedOperationException();
    }
}
