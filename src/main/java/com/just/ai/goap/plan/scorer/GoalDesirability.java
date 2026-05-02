package com.just.ai.goap.plan.scorer;

import com.just.ai.goap.goal.Goal;
import com.just.ai.goap.state.ReadableWorldState;

@FunctionalInterface
public interface GoalDesirability<T> {

    float getDesirability(Goal goal, T actor, ReadableWorldState worldState);
}
