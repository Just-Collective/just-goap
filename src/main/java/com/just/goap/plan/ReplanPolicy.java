package com.just.goap.plan;

import com.just.goap.Agent;
import com.just.goap.graph.Graph;
import com.just.goap.state.ReadableWorldState;

/**
 * Determines when an agent should request new plans.
 *
 * @param <T> The actor type.
 */
@FunctionalInterface
public interface ReplanPolicy<T> {

    /**
     * Evaluates whether the agent should request new plans.
     *
     * @param context Information about the current state.
     * @return true if the agent should request plans.
     */
    boolean shouldReplan(Context<T> context);

    /**
     * Context provided to a {@link ReplanPolicy} for evaluation.
     *
     * @param <T> The actor type.
     */
    record Context<T>(
        Agent<T> agent,
        Graph<T> graph,
        ReadableWorldState worldState,
        ReadableWorldState previousWorldState
    ) {}
}
