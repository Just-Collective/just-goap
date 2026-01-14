package com.just.goap.plan;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.just.goap.AOStar;
import com.just.goap.graph.Graph;
import com.just.goap.state.ReadableWorldState;

public class DefaultPlanFactory {

    public static <T> List<Plan<T>> create(Graph<T> graph, T context, ReadableWorldState worldState) {
        var plans = new ArrayList<PlanWithCost<T>>();

        for (var goal : graph.getAvailableGoals()) {
            if (!goal.getPreconditions().satisfiedBy(worldState)) {
                // Goal's preconditions are not satisfied by the current world state, skip the goal.
                continue;
            }

            // We need to find actions that satisfy these conditions.
            var desiredConditions = goal.getDesiredConditions();

            var actions = AOStar.solve(graph, desiredConditions, worldState, context);

            if (actions != null && !actions.isEmpty()) {
                // FIXME: The cost-per-action here is evaluated using the wrong world state. We need to use the
                // simulated state.
                var cost = 0.0f;
                for (var action : actions) {
                    cost += action.getCost(context, worldState);
                }

                plans.add(new PlanWithCost<>(new Plan<>(goal, actions), cost));
            }
        }

        // Sort by cost (lowest first) and extract plans.
        plans.sort(Comparator.comparingDouble(p -> p.cost));

        var result = new ArrayList<Plan<T>>(plans.size());

        for (var planWithCost : plans) {
            result.add(planWithCost.plan);
        }

        return result;
    }

    private record PlanWithCost<T>(
        Plan<T> plan,
        float cost
    ) {}

    private DefaultPlanFactory() {
        throw new UnsupportedOperationException();
    }
}
