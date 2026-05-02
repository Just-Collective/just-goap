package com.just.ai.goap.plan.scorer;

import java.util.List;

import com.just.ai.goap.plan.Plan;
import com.just.ai.goap.state.ReadableWorldState;

@FunctionalInterface
public interface PlanScorer<T> {

    float score(Plan<T> plan, T actor, ReadableWorldState worldState, List<Plan<T>> allPlans);
}
