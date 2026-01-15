package com.just.goap.plan;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.just.goap.AOStar;
import com.just.goap.Agent;
import com.just.goap.graph.Graph;
import com.just.goap.state.ReadableWorldState;

public class DefaultPlanFactory {

    public static <T> List<Plan<T>> create(
        Graph<T> graph,
        T actor,
        ReadableWorldState worldState,
        Agent.Debugger debugger
    ) {
        var plans = new ArrayList<PlanWithCost<T>>();

        for (var goal : graph.getAvailableGoals()) {
            debugger.push("Goal '" + goal.getName() + "' precondition check");
            var preconditionsSatisfied = goal.getPreconditions().satisfiedBy(worldState);
            debugger.pop();

            if (!preconditionsSatisfied) {
                continue;
            }

            // We need to find actions that satisfy these conditions.
            var desiredConditions = goal.getDesiredConditions();

            debugger.push("AOStar.solve() for goal '" + goal.getName() + "'");
            var actions = AOStar.solve(graph, desiredConditions, worldState, actor);
            debugger.pop();

            if (actions != null && !actions.isEmpty()) {
                // FIXME: The cost-per-action here is evaluated using the wrong world state. We need to use the
                // simulated state.
                var cost = 0.0f;
                for (var action : actions) {
                    cost += action.getCost(actor, worldState);
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
