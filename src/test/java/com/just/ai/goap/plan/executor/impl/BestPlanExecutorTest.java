package com.just.ai.goap.plan.executor.impl;

import org.junit.jupiter.api.Test;

import java.util.List;

import com.just.ai.goap.StateKey;
import com.just.ai.goap.action.Action;
import com.just.ai.goap.condition.expression.Expressions;
import com.just.ai.goap.goal.Goal;
import com.just.ai.goap.plan.Plan;
import com.just.ai.goap.plan.scorer.PlanScorers;
import com.just.ai.goap.state.WorldState;

import static org.junit.jupiter.api.Assertions.assertSame;

class BestPlanExecutorTest {

    private static final StateKey.Derived<Boolean> TEST_STATE = StateKey.derived("executor_test_state");

    @Test
    void supplyPlansSelectsHighestScoringPlan() {
        var cheapLowDesirability = plan("low", 1f);
        var costlyHighDesirability = plan("high", 10f);
        var scorer = PlanScorers.<Object>goalDesirability(
            (goal, actor, worldState) -> goal.getName().equals("high") ? 1f : 0f
        );
        var executor = new BestPlanExecutor<>(scorer);

        executor.supplyPlans(List.of(cheapLowDesirability, costlyHighDesirability), new Object(), WorldState.create());

        assertSame(costlyHighDesirability, executor.getCurrentPlan());
    }

    @Test
    void defaultExecutorStillSelectsCheapestPlan() {
        var cheap = plan("cheap", 1f);
        var expensive = plan("expensive", 10f);
        var executor = new BestPlanExecutor<>();

        executor.supplyPlans(List.of(expensive, cheap), new Object(), WorldState.create());

        assertSame(cheap, executor.getCurrentPlan());
    }

    private static Plan<Object> plan(String goalName, float initialCost) {
        return new Plan<>(goal(goalName), List.of(action(goalName + "Action")), initialCost);
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
