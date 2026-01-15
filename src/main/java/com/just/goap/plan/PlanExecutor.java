package com.just.goap.plan;

import java.util.List;

import com.just.goap.Agent;
import com.just.goap.state.ReadableWorldState;

public interface PlanExecutor<T> {

    /**
     * Executes the current plan(s) for one tick.
     *
     * @param context The execution context containing agent, actor, and world states.
     * @return The result of this execution tick.
     */
    Result execute(ExecutionContext<T> context);

    /**
     * Provides new plans to the executor. Called by the Agent when {@link #needsPlans()} returns true.
     *
     * @param plans The available plans, sorted by cost (lowest first).
     */
    void supplyPlans(List<Plan<T>> plans);

    /**
     * Returns true if the executor needs new plans from the plan factory. The Agent will call the PlanFactory and then
     * {@link #supplyPlans(List)} when this returns true.
     */
    boolean needsPlans();

    /**
     * Returns true if the executor has at least one active plan being executed.
     */
    boolean hasActivePlans();

    /**
     * Abandons all currently active plans. Called when the agent needs to force a replan (e.g., due to external
     * events).
     */
    void abandonAllPlans();

    /**
     * The result of a single execution tick.
     */
    enum Result {
        /**
         * At least one plan is still in progress.
         */
        IN_PROGRESS,

        /**
         * All plans have completed (finished, aborted, or invalidated). The executor will need new plans.
         */
        IDLE,

        /**
         * No plans were available to execute.
         */
        NO_PLANS
    }

    /**
     * Context provided to the executor for each execution tick.
     */
    record ExecutionContext<T>(
        Agent<T> agent,
        T actor,
        ReadableWorldState currentWorldState,
        ReadableWorldState previousWorldState
    ) {}
}
