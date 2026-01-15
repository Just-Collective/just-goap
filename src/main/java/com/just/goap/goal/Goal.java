package com.just.goap.goal;

import com.just.goap.condition.ConditionContainer;

/**
 * Represents a goal that the GOAP agent wants to achieve.
 * <p>
 * Goals define:
 * <ul>
 * <li><b>Desired conditions</b>: The state the agent wants to reach (derived keys only)</li>
 * <li><b>Preconditions</b>: Conditions that must be true to consider this goal (sensed keys only)</li>
 * </ul>
 */
public interface Goal {

    /**
     * Creates a new goal builder.
     *
     * @param name The name of the goal.
     * @return A new builder instance.
     */
    static BaseGoal.ConcreteBuilder builder(String name) {
        return BaseGoal.builder(name);
    }

    /**
     * Returns the desired conditions that define what this goal wants to achieve. These conditions use derived keys and
     * are satisfied by action effects.
     */
    ConditionContainer getDesiredConditions();

    /**
     * Returns the name of this goal.
     */
    String getName();

    /**
     * Returns the preconditions that must be true for this goal to be considered. These conditions use sensed keys and
     * are evaluated against the current world state.
     */
    ConditionContainer getPreconditions();
}
