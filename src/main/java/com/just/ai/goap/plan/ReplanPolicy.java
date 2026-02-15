package com.just.ai.goap.plan;

import com.just.ai.goap.Agent;
import com.just.ai.goap.graph.Graph;
import com.just.ai.goap.state.ReadableWorldState;

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
