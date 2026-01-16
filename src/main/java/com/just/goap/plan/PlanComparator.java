package com.just.goap.plan;

import java.util.List;

import com.just.goap.action.Action;

/**
 * Utility class for comparing plans.
 */
public final class PlanComparator {

    private PlanComparator() {}

    /**
     * Returns {@code true} if the two plans have the same goal.
     *
     * @param planA The first plan.
     * @param planB The second plan.
     * @param <T>   The actor type.
     * @return {@code true} if the plans have the same goal.
     */
    public static <T> boolean hasSameGoal(Plan<T> planA, Plan<T> planB) {
        if (planA == planB) {
            return true;
        }

        if (planA == null || planB == null) {
            return false;
        }

        return planA.getGoal().equals(planB.getGoal());
    }

    /**
     * Returns {@code true} if the two plans have the same goal and the same action sequence.
     * <p>
     * Action equality is determined by reference equality (same action instance).
     *
     * @param planA The first plan.
     * @param planB The second plan.
     * @param <T>   The actor type.
     * @return {@code true} if the plans have the same goal and actions.
     */
    public static <T> boolean hasSameGoalAndActions(Plan<T> planA, Plan<T> planB) {
        if (planA == planB) {
            return true;
        }

        if (planA == null || planB == null) {
            return false;
        }

        if (!planA.getGoal().equals(planB.getGoal())) {
            return false;
        }

        return hasSameActions(planA.getActions(), planB.getActions());
    }

    /**
     * Returns {@code true} if the two action lists contain the same actions in the same order.
     * <p>
     * Action equality is determined by reference equality (same action instance).
     *
     * @param actionsA The first action list.
     * @param actionsB The second action list.
     * @param <T>      The actor type.
     * @return {@code true} if the action lists are equal.
     */
    public static <T> boolean hasSameActions(List<Action<? super T>> actionsA, List<Action<? super T>> actionsB) {
        if (actionsA == actionsB) {
            return true;
        }

        if (actionsA.size() != actionsB.size()) {
            return false;
        }

        for (int i = 0; i < actionsA.size(); i++) {
            if (actionsA.get(i) != actionsB.get(i)) {
                return false;
            }
        }

        return true;
    }
}
