package com.just.goap;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

import com.just.goap.condition.ConditionContainer;
import com.just.goap.graph.Graph;
import com.just.goap.state.ReadableWorldState;
import com.just.goap.state.SensingWorldState;
import com.just.goap.state.SimulatedWorldState;

public final class AOStar {

    private static final Logger LOGGER = LoggerFactory.getLogger(AOStar.class);

    public static <T> @Nullable List<Action<T>> solve(
        Graph<T> graph,
        ConditionContainer desiredConditions,
        SensingWorldState<T> startState,
        T context
    ) {
        var open = new PriorityQueue<AOStarNode<T>>(Comparator.comparingDouble(node -> node.fCost));

        var rootUnsatisfied = desiredConditions.filterUnsatisfied(startState);
        var rootState = new SimulatedWorldState<>(startState);

        LOGGER.trace("Start state: {}", startState);
        LOGGER.trace("Root unsatisfied conditions: {}", rootUnsatisfied.getConditions());

        open.add(
            new AOStarNode<>(
                rootUnsatisfied,
                new ArrayList<>(),
                rootState,
                0.0f,
                heuristic(rootUnsatisfied, graph, context, startState)
            )
        );

        while (!open.isEmpty()) {
            var node = open.poll();

            LOGGER.trace("\n--- Expanding node ---");
            LOGGER.trace("Plan so far: {}", node.planSoFar);
            LOGGER.trace("Unsatisfied conditions: {}", node.unsatisfiedConditions.getConditions());
            LOGGER.trace("g={} h={} f={}", node.gCost, node.hCost, node.fCost);

            if (node.unsatisfiedConditions.isEmpty()) {
                LOGGER.trace("Goal reached! Returning plan.");
                // All conditions are satisfied, return the plan.
                return node.planSoFar.reversed();
            }

            for (var condition : node.unsatisfiedConditions.getConditions()) {
                LOGGER.trace("Expanding condition: {}", condition);

                var satisfyingActions = graph.getActionsThatSatisfy(condition);
                LOGGER.trace("Candidate actions: {}", satisfyingActions);

                for (var action : satisfyingActions) {
                    LOGGER.trace(" Trying action: {}", action);
                    // Simulate applying the action
                    var newState = node.simulatedState.copy();
                    newState.apply(action.getEffectContainer());
                    LOGGER.trace("  Applied effects, new state: {}", newState);

                    // Collect remaining unsatisfied conditions (action’s preconditions + what was left).

                    // Preconditions must be true before the action runs
                    var unmetPreconditions = action.getPreconditionContainer()
                        .filterUnsatisfied(node.simulatedState);

                    // Remaining desired conditions that weren’t satisfied by this action.
                    var remaining = node.unsatisfiedConditions
                        .without(condition)
                        .filterUnsatisfied(newState);

                    // Union what’s left of the original goals + action’s unmet preconditions.
                    var newUnsatisfied = remaining.union(unmetPreconditions);

                    LOGGER.trace("  New unsatisfied after action: {}", newUnsatisfied.getConditions());

                    // Build plan so far
                    var newPlan = new ArrayList<>(node.planSoFar);
                    newPlan.add(action);

                    // Costs
                    var g = node.gCost + action.getCost(context, node.simulatedState);
                    var h = heuristic(newUnsatisfied, graph, context, node.simulatedState);
                    LOGGER.trace(
                        "  Action cost={} → g={} h={} f={}",
                        action.getCost(context, node.simulatedState),
                        g,
                        h,
                        g + h
                    );

                    open.add(new AOStarNode<>(newUnsatisfied, newPlan, newState, g, h));
                }
            }
        }

        LOGGER.trace("No plan found.");
        return null;
    }

    private static <T> float heuristic(
        ConditionContainer unsatisfied,
        Graph<T> graph,
        T context,
        ReadableWorldState state
    ) {
        var h = 0.0f;

        for (var condition : unsatisfied.getConditions()) {
            var candidates = graph.getActionsThatSatisfy(condition);

            if (!candidates.isEmpty()) {
                var minCost = candidates.stream()
                    .map(a -> a.getCost(context, state))
                    .min(Float::compare)
                    .orElse(Float.MAX_VALUE);

                h += minCost;
            } else {
                LOGGER.trace(" Heuristic: condition {} has no satisfiers → returning ∞", condition);
                // No known action can satisfy this condition.
                return Float.MAX_VALUE;
            }
        }

        return h;
    }

    record AOStarNode<T>(
        ConditionContainer unsatisfiedConditions,
        List<Action<T>> planSoFar,
        SimulatedWorldState<T> simulatedState,
        // cost so far
        float gCost,
        // heuristic estimate
        float hCost,
        // g + h
        float fCost
    ) {

        AOStarNode(
            ConditionContainer unsatisfiedConditions,
            List<Action<T>> planSoFar,
            SimulatedWorldState<T> simulatedState,
            float gCost,
            float hCost
        ) {
            this(unsatisfiedConditions, planSoFar, simulatedState, gCost, hCost, gCost + hCost);
        }
    }
}
