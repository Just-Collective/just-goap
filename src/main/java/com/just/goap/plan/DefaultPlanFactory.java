package com.just.goap.plan;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.just.goap.AOStar;
import com.just.goap.Action;
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
            var actionsWithCosts = AOStar.solve(graph, desiredConditions, worldState, actor);
            debugger.pop();

            if (actionsWithCosts != null && !actionsWithCosts.isEmpty()) {
                // Sum the costs already computed by AOStar (using correct simulated world states).
                var cost = 0.0f;
                // Extract just the actions for the plan.
                var actions = new ArrayList<Action<? super T>>(actionsWithCosts.size());

                for (var actionWithCost : actionsWithCosts) {
                    cost += actionWithCost.cost();
                    actions.add(actionWithCost.action());
                }

                plans.add(new PlanWithCost<>(new Plan<>(goal, actions), cost));
            }
        }

        // Sort by cost (lowest first) and extract plans.
        plans.sort(Comparator.comparingDouble(planWithCost -> planWithCost.cost));

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
