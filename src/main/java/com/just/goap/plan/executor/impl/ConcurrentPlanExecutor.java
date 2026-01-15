package com.just.goap.plan.executor.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.just.goap.plan.Plan;
import com.just.goap.plan.executor.PlanExecutor;

/**
 * A plan executor that runs multiple non-conflicting plans concurrently.
 * <p>
 * This executor uses a {@link ConflictDetector} to determine which plans can run together. Plans that don't conflict
 * with any active plan are started immediately.
 * <p>
 * The executor can be configured with:
 * <ul>
 * <li>A conflict detector (default: allow all plans)</li>
 * <li>A maximum number of concurrent plans (default: unlimited)</li>
 * </ul>
 * <p>
 * Example usage with custom conflict detection:
 *
 * <pre>{@code
 *
 * // Define a conflict detector based on your custom action metadata
 * PlanConflictDetector<Entity> detector = (planA, planB) -> {
 *     var capabilitiesA = extractCapabilities(planA);
 *     var capabilitiesB = extractCapabilities(planB);
 *     return !Collections.disjoint(capabilitiesA, capabilitiesB);
 * };
 *
 * var executor = ConcurrentPlanExecutor.<Entity>builder()
 *     .withConflictDetector(detector)
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
     * Creates a ConcurrentPlanExecutor with default settings (no conflict detection, unlimited plans).
     */
    public static <T> ConcurrentPlanExecutor<T> create() {
        return new ConcurrentPlanExecutor<>(ConflictDetector.allowAll(), Integer.MAX_VALUE);
    }

    private final List<Plan<T>> activePlans;

    private final ConflictDetector<T> conflictDetector;

    private final int maxConcurrentPlans;

    private ConcurrentPlanExecutor(ConflictDetector<T> conflictDetector, int maxConcurrentPlans) {
        this.activePlans = new ArrayList<>();
        this.conflictDetector = conflictDetector;
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

            if (canAddPlan(candidate)) {
                activePlans.add(candidate);
            }
        }
    }

    /**
     * Checks if a candidate plan can be added without conflicting with active plans.
     */
    private boolean canAddPlan(Plan<T> candidate) {
        for (var active : activePlans) {
            if (conflictDetector.conflicts(candidate, active)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean needsPlans() {
        // Request new plans when we have no active plans
        // Subclasses can override to request plans more aggressively
        return activePlans.isEmpty();
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
     * Returns the conflict detector used by this executor.
     */
    public ConflictDetector<T> getConflictDetector() {
        return conflictDetector;
    }

    /**
     * Determines whether two plans conflict and cannot run concurrently.
     * <p>
     * Implement this interface to define custom conflict detection logic for use with {@link ConcurrentPlanExecutor}.
     * The executor will check each candidate plan against all currently active plans using this detector.
     * <p>
     * Example implementation for resource-based conflicts:
     *
     * <pre>{@code
     *
     * PlanConflictDetector<Entity> resourceConflict = (planA, planB) -> {
     *     var resourcesA = extractRequiredResources(planA);
     *     var resourcesB = extractRequiredResources(planB);
     *     return !Collections.disjoint(resourcesA, resourcesB);
     * };
     * }</pre>
     *
     * @param <T> The actor type.
     */
    @FunctionalInterface
    public interface ConflictDetector<T> {

        /**
         * A detector that reports no conflicts, allowing all plans to run concurrently.
         */
        @SuppressWarnings("unchecked")
        static <T> ConflictDetector<T> allowAll() {
            return (ConflictDetector<T>) AllowAll.INSTANCE;
        }

        /**
         * Returns {@code true} if the two plans conflict and cannot run concurrently, {@code false} if they can run
         * together.
         *
         * @param planA The first plan.
         * @param planB The second plan.
         * @return {@code true} if the plans conflict.
         */
        boolean conflicts(Plan<T> planA, Plan<T> planB);

        /**
         * Returns a new detector that reports a conflict if either this detector or the other detector reports a
         * conflict.
         *
         * @param other The other detector to combine with.
         * @return A combined detector.
         */
        default ConflictDetector<T> or(ConflictDetector<T> other) {
            return (planA, planB) -> this.conflicts(planA, planB) || other.conflicts(planA, planB);
        }

        /**
         * Returns a new detector that reports a conflict only if both this detector and the other detector report a
         * conflict.
         *
         * @param other The other detector to combine with.
         * @return A combined detector.
         */
        default ConflictDetector<T> and(ConflictDetector<T> other) {
            return (planA, planB) -> this.conflicts(planA, planB) && other.conflicts(planA, planB);
        }

        /**
         * Internal singleton for the allow-all detector.
         */
        enum AllowAll implements ConflictDetector<Object> {

            INSTANCE;

            @Override
            public boolean conflicts(Plan<Object> planA, Plan<Object> planB) {
                return false;
            }
        }

    }

    /**
     * Builder for configuring a {@link ConcurrentPlanExecutor}.
     *
     * @param <T> The actor type.
     */
    public static class Builder<T> {

        private ConflictDetector<T> conflictDetector;

        private int maxConcurrentPlans;

        private Builder() {
            this.conflictDetector = ConflictDetector.allowAll();
            this.maxConcurrentPlans = Integer.MAX_VALUE;
        }

        /**
         * Sets the conflict detector used to determine which plans can run concurrently.
         *
         * @param conflictDetector The conflict detector.
         * @return This builder.
         */
        public Builder<T> withConflictDetector(ConflictDetector<T> conflictDetector) {
            this.conflictDetector = conflictDetector;
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
            return new ConcurrentPlanExecutor<>(conflictDetector, maxConcurrentPlans);
        }
    }
}
