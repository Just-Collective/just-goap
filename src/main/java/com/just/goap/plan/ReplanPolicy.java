package com.just.goap.plan;

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
     * @param <T>            The actor type.
     * @param hasActivePlans Whether the executor currently has active plans.
     */
    record Context<T>(boolean hasActivePlans) {}
}
