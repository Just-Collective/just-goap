package com.just.ai.goap.plan.executor.impl;

import org.junit.jupiter.api.Test;

import java.util.List;

import com.just.ai.goap.StateKey;
import com.just.ai.goap.action.Action;
import com.just.ai.goap.condition.expression.Expressions;
import com.just.ai.goap.goal.Goal;
import com.just.ai.goap.plan.Plan;
import com.just.ai.goap.plan.executor.impl.ConcurrentPlanExecutor.PlanResolver;
import com.just.ai.goap.plan.executor.impl.ConcurrentPlanExecutor.PlanResolver.Resolution;
import com.just.ai.goap.plan.scorer.PlanScorers;
import com.just.ai.goap.state.WorldState;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class ConcurrentPlanExecutorTest {

    private static final StateKey.Derived<Boolean> TEST_STATE = StateKey.derived("concurrent_executor_test_state");

    @Test
    void preferHigherScoreReplacesLowerScoringPlanForSameGoal() {
        var goal = goal("same");
        var active = plan(goal, "active", 10f);
        var candidate = plan(goal, "candidate", 1f);
        var resolver = PlanResolver.preferHigherScore(PlanScorers.<Object>costEfficiency());

        var resolution = resolver.resolve(active, candidate, new Object(), WorldState.create());

        assertEquals(Resolution.REPLACE_ACTIVE, resolution);
    }

    @Test
    void preferHigherScoreAllowsDifferentGoals() {
        var active = plan(goal("active"), "activeAction", 10f);
        var candidate = plan(goal("candidate"), "candidateAction", 1f);
        var resolver = PlanResolver.preferHigherScore(PlanScorers.<Object>costEfficiency());

        var resolution = resolver.resolve(active, candidate, new Object(), WorldState.create());

        assertEquals(Resolution.NO_CONFLICT, resolution);
    }

    @Test
    void executorUsesHigherScoreResolverWhenProcessingCandidates() {
        var goal = goal("same");
        var active = plan(goal, "active", 10f);
        var candidate = plan(goal, "candidate", 1f);
        var executor = ConcurrentPlanExecutor.<Object>builder()
            .withPlanResolver(PlanResolver.preferHigherScore(PlanScorers.costEfficiency()))
            .build();

        executor.supplyPlans(List.of(active), new Object(), WorldState.create());
        executor.supplyPlans(List.of(candidate), new Object(), WorldState.create());

        assertEquals(1, executor.getActivePlanCount());
        assertSame(candidate, executor.getActivePlans().getFirst());
    }

    private static Plan<Object> plan(Goal goal, String actionName, float initialCost) {
        return new Plan<>(goal, List.of(action(actionName)), initialCost);
    }

    private static Goal goal(String name) {
        return Goal.builder(name)
            .addDesiredCondition(TEST_STATE, Expressions.Boolean.isTrue())
            .build();
    }

    private static Action<Object> action(String name) {
        return Action.builder(name)
            .addEffect(TEST_STATE, true)
            .build();
    }
}
