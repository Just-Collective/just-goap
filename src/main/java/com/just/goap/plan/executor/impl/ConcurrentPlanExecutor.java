package com.just.goap.plan.executor.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.just.goap.plan.Plan;
import com.just.goap.plan.PlanComparator;
import com.just.goap.plan.executor.PlanExecutor;

/**
 * A plan executor that runs multiple non-conflicting plans concurrently.
 * <p>
 * This executor uses a {@link PlanResolver} to determine how to handle conflicts between plans. When a candidate plan
 * conflicts with an active plan, the resolver decides whether to keep the active plan, replace it with the candidate,
 * reject both, or allow both to run concurrently.
 * <p>
 * The executor can be configured with:
 * <ul>
 * <li>A plan resolver (default: prefer cheaper plan for same goal)</li>
 * <li>A maximum number of concurrent plans (default: unlimited)</li>
 * </ul>
 * <p>
 * Example usage with custom resolution:
 *
 * <pre>{@code
 *
 * PlanResolver<Entity> resolver = (active, candidate) -> {
 *     if (hasResourceConflict(active, candidate)) {
 *         // Keep whichever plan is cheaper
 *         return candidate.getCost() < active.getCost()
 *             ? Resolution.REPLACE_ACTIVE
 *             : Resolution.KEEP_ACTIVE;
 *     }
 *     return Resolution.NO_CONFLICT;
 * };
 *
 * var executor = ConcurrentPlanExecutor.<Entity>builder()
 *     .withPlanResolver(resolver)
 *     .withMaxConcurrentPlans(3)
 *     .build();
 *
 * var agent = Agent.<Entity>builder()
 *     .withPlanExecutor(executor)
 *     .build();
 * }</pre>
 *
 * @param <T> The actor type.
 */
public class ConcurrentPlanExecutor<T> implements PlanExecutor<T> {

    /**
     * Creates a new builder for configuring a ConcurrentPlanExecutor.
     */
    public static <T> Builder<T> builder() {
        return new Builder<>();
    }

    /**
     * Creates a ConcurrentPlanExecutor with default settings (prefer cheaper same-goal plans, unlimited plans).
     */
    public static <T> ConcurrentPlanExecutor<T> create() {
        return new ConcurrentPlanExecutor<>(PlanResolver.preferCheaperSameGoal(), Integer.MAX_VALUE);
    }

    private final List<Plan<T>> activePlans;

    private final PlanResolver<T> planResolver;

    private final int maxConcurrentPlans;

    private ConcurrentPlanExecutor(PlanResolver<T> planResolver, int maxConcurrentPlans) {
        this.activePlans = new ArrayList<>();
        this.planResolver = planResolver;
        this.maxConcurrentPlans = maxConcurrentPlans;
    }

    @Override
    public Result execute(ExecutionContext<T> context) {
        if (activePlans.isEmpty()) {
            return Result.NO_PLANS;
        }

        var debugger = context.agent().getDebugger();
        debugger.push("ConcurrentPlanExecutor.execute()");

        Iterator<Plan<T>> iterator = activePlans.iterator();
        boolean anyInProgress = false;

        while (iterator.hasNext()) {
            var plan = iterator.next();

            var planState = plan.update(
                context.agent(),
                context.actor(),
                context.currentWorldState(),
                context.previousWorldState()
            );

            switch (planState) {
                case ABORTED, FINISHED, INVALID -> iterator.remove();
                case IN_PROGRESS -> anyInProgress = true;
            }
        }

        debugger.pop();

        return anyInProgress ? Result.IN_PROGRESS : Result.IDLE;
    }

    @Override
    public void supplyPlans(List<Plan<T>> plans) {
        for (var candidate : plans) {
            if (activePlans.size() >= maxConcurrentPlans) {
                break;
            }

            processCandidate(candidate);
        }
    }

    /**
     * Processes a candidate plan by resolving conflicts with active plans.
     */
    private void processCandidate(Plan<T> candidate) {
        List<Plan<T>> toRemove = null;
        boolean acceptCandidate = true;

        for (var active : activePlans) {
            var resolution = planResolver.resolve(active, candidate);

            switch (resolution) {
                case KEEP_ACTIVE -> {
                    // Reject candidate, keep active - stop processing this candidate
                    acceptCandidate = false;
                }
                case REPLACE_ACTIVE -> {
                    // Accept candidate, remove active
                    if (toRemove == null) {
                        toRemove = new ArrayList<>();
                    }

                    toRemove.add(active);
                }
                case REJECT_BOTH -> {
                    // Reject candidate and remove active
                    acceptCandidate = false;

                    if (toRemove == null) {
                        toRemove = new ArrayList<>();
                    }

                    toRemove.add(active);
                }
                case NO_CONFLICT -> {
                    // Continue checking other active plans
                }
            }

            if (!acceptCandidate && resolution != PlanResolver.Resolution.REJECT_BOTH) {
                // If we're rejecting the candidate (but not removing active plans), stop early
                break;
            }
        }

        // Remove any plans marked for removal
        if (toRemove != null) {
            activePlans.removeAll(toRemove);
        }

        // Add candidate if accepted and we have capacity
        if (acceptCandidate && activePlans.size() < maxConcurrentPlans) {
            activePlans.add(candidate);
        }
    }

    @Override
    public boolean hasActivePlans() {
        return !activePlans.isEmpty();
    }

    @Override
    public void abandonAllPlans() {
        activePlans.clear();
    }

    /**
     * Returns an unmodifiable view of the currently active plans.
     */
    public List<Plan<T>> getActivePlans() {
        return Collections.unmodifiableList(activePlans);
    }

    /**
     * Returns the number of currently active plans.
     */
    public int getActivePlanCount() {
        return activePlans.size();
    }

    /**
     * Returns the maximum number of concurrent plans allowed.
     */
    public int getMaxConcurrentPlans() {
        return maxConcurrentPlans;
    }

    /**
     * Returns the plan resolver used by this executor.
     */
    public PlanResolver<T> getPlanResolver() {
        return planResolver;
    }

    /**
     * Resolves conflicts between an active plan and an incoming candidate plan.
     * <p>
     * Implement this interface to define custom resolution logic for use with {@link ConcurrentPlanExecutor}. The
     * executor will check each candidate plan against all currently active plans using this resolver.
     *
     * @param <T> The actor type.
     */
    @FunctionalInterface
    public interface PlanResolver<T> {

        /**
         * A resolver that reports no conflicts, allowing all plans to run concurrently.
         */
        @SuppressWarnings("unchecked")
        static <T> PlanResolver<T> allowAll() {
            return (PlanResolver<T>) AllowAll.INSTANCE;
        }

        /**
         * A resolver that rejects candidates with the same goal as an active plan. The active plan is always kept.
         */
        @SuppressWarnings("unchecked")
        static <T> PlanResolver<T> rejectSameGoal() {
            return (PlanResolver<T>) RejectSameGoal.INSTANCE;
        }

        /**
         * A resolver that prefers the cheaper plan when two plans have the same goal. If the candidate is cheaper, it
         * replaces the active plan. Otherwise, the candidate is rejected.
         */
        @SuppressWarnings("unchecked")
        static <T> PlanResolver<T> preferCheaperSameGoal() {
            return (PlanResolver<T>) PreferCheaperSameGoal.INSTANCE;
        }

        /**
         * Resolves a conflict between an active plan and an incoming candidate plan.
         *
         * @param active    The currently running plan.
         * @param candidate The incoming plan being considered.
         * @return The resolution decision.
         */
        Resolution resolve(Plan<T> active, Plan<T> candidate);

        /**
         * The resolution decision for a conflict between two plans.
         */
        enum Resolution {
            /**
             * Keep the active plan, reject the candidate.
             */
            KEEP_ACTIVE,

            /**
             * Replace the active plan with the candidate.
             */
            REPLACE_ACTIVE,

            /**
             * Reject both the active plan and the candidate.
             */
            REJECT_BOTH,

            /**
             * Keep the active plan and also accept the candidate (no conflict).
             */
            NO_CONFLICT
        }

        /**
         * Internal singleton for the allow-all resolver.
         */
        enum AllowAll implements PlanResolver<Object> {

            INSTANCE;

            @Override
            public Resolution resolve(Plan<Object> active, Plan<Object> candidate) {
                return Resolution.NO_CONFLICT;
            }
        }

        /**
         * Internal singleton for the reject-same-goal resolver.
         */
        enum RejectSameGoal implements PlanResolver<Object> {

            INSTANCE;

            @Override
            public Resolution resolve(Plan<Object> active, Plan<Object> candidate) {
                if (PlanComparator.hasSameGoal(active, candidate)) {
                    return Resolution.KEEP_ACTIVE;
                }

                return Resolution.NO_CONFLICT;
            }
        }

        /**
         * Internal singleton for the prefer-cheaper-same-goal resolver.
         */
        enum PreferCheaperSameGoal implements PlanResolver<Object> {

            INSTANCE;

            @Override
            public Resolution resolve(Plan<Object> active, Plan<Object> candidate) {
                if (!PlanComparator.hasSameGoal(active, candidate)) {
                    return Resolution.NO_CONFLICT;
                }

                // Same goal - keep the cheaper one
                return candidate.getCost() < active.getCost()
                    ? Resolution.REPLACE_ACTIVE
                    : Resolution.KEEP_ACTIVE;
            }
        }
    }

    /**
     * Builder for configuring a {@link ConcurrentPlanExecutor}.
     *
     * @param <T> The actor type.
     */
    public static class Builder<T> {

        private PlanResolver<T> planResolver;

        private int maxConcurrentPlans;

        private Builder() {
            this.planResolver = PlanResolver.preferCheaperSameGoal();
            this.maxConcurrentPlans = Integer.MAX_VALUE;
        }

        /**
         * Sets the plan resolver used to determine how to handle conflicts between plans.
         *
         * @param planResolver The plan resolver.
         * @return This builder.
         */
        public Builder<T> withPlanResolver(PlanResolver<T> planResolver) {
            this.planResolver = planResolver;
            return this;
        }

        /**
         * Sets the maximum number of plans that can run concurrently.
         *
         * @param maxConcurrentPlans The maximum number of concurrent plans.
         * @return This builder.
         */
        public Builder<T> withMaxConcurrentPlans(int maxConcurrentPlans) {
            if (maxConcurrentPlans < 1) {
                throw new IllegalArgumentException("maxConcurrentPlans must be at least 1");
            }

            this.maxConcurrentPlans = maxConcurrentPlans;
            return this;
        }

        /**
         * Builds the configured ConcurrentPlanExecutor.
         *
         * @return The configured executor.
         */
        public ConcurrentPlanExecutor<T> build() {
            return new ConcurrentPlanExecutor<>(planResolver, maxConcurrentPlans);
        }
    }
}
