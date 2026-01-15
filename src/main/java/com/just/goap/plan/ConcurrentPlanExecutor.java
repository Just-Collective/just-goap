package com.just.goap.plan;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * A plan executor that runs multiple non-conflicting plans concurrently.
 * <p>
 * This executor uses a {@link PlanConflictDetector} to determine which plans can run together. Plans that don't
 * conflict with any active plan are started immediately.
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
        return new ConcurrentPlanExecutor<>(PlanConflictDetector.allowAll(), Integer.MAX_VALUE);
    }

    private final List<Plan<T>> activePlans;

    private final PlanConflictDetector<T> conflictDetector;

    private final int maxConcurrentPlans;

    private ConcurrentPlanExecutor(PlanConflictDetector<T> conflictDetector, int maxConcurrentPlans) {
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
    public PlanConflictDetector<T> getConflictDetector() {
        return conflictDetector;
    }

    /**
     * Builder for configuring a {@link ConcurrentPlanExecutor}.
     *
     * @param <T> The actor type.
     */
    public static class Builder<T> {

        private PlanConflictDetector<T> conflictDetector;

        private int maxConcurrentPlans;

        private Builder() {
            this.conflictDetector = PlanConflictDetector.allowAll();
            this.maxConcurrentPlans = Integer.MAX_VALUE;
        }

        /**
         * Sets the conflict detector used to determine which plans can run concurrently.
         *
         * @param conflictDetector The conflict detector.
         * @return This builder.
         */
        public Builder<T> withConflictDetector(PlanConflictDetector<T> conflictDetector) {
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
