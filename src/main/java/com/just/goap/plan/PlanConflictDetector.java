package com.just.goap.plan;

/**
 * Determines whether two plans conflict and cannot run concurrently.
 * <p>
 * Implement this interface to define custom conflict detection logic for use with {@link ConcurrentPlanExecutor}. The
 * executor will check each candidate plan against all currently active plans using this detector.
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
public interface PlanConflictDetector<T> {

    /**
     * A detector that reports no conflicts, allowing all plans to run concurrently.
     */
    @SuppressWarnings("unchecked")
    static <T> PlanConflictDetector<T> allowAll() {
        return (PlanConflictDetector<T>) AllowAll.INSTANCE;
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
     * Returns a new detector that reports a conflict if either this detector or the other detector reports a conflict.
     *
     * @param other The other detector to combine with.
     * @return A combined detector.
     */
    default PlanConflictDetector<T> or(PlanConflictDetector<T> other) {
        return (planA, planB) -> this.conflicts(planA, planB) || other.conflicts(planA, planB);
    }

    /**
     * Returns a new detector that reports a conflict only if both this detector and the other detector report a
     * conflict.
     *
     * @param other The other detector to combine with.
     * @return A combined detector.
     */
    default PlanConflictDetector<T> and(PlanConflictDetector<T> other) {
        return (planA, planB) -> this.conflicts(planA, planB) && other.conflicts(planA, planB);
    }
}

/**
 * Internal singleton for the allow-all detector.
 */
enum AllowAll implements PlanConflictDetector<Object> {

    INSTANCE;

    @Override
    public boolean conflicts(Plan<Object> planA, Plan<Object> planB) {
        return false;
    }
}
