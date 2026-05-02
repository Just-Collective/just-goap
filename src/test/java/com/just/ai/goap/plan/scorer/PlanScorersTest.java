package com.just.ai.goap.plan.scorer;

import org.junit.jupiter.api.Test;

import java.util.List;

import com.just.ai.goap.StateKey;
import com.just.ai.goap.action.Action;
import com.just.ai.goap.condition.expression.Expressions;
import com.just.ai.goap.goal.Goal;
import com.just.ai.goap.plan.Plan;
import com.just.ai.goap.state.WorldState;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PlanScorersTest {

    private static final StateKey.Derived<Boolean> TEST_STATE = StateKey.derived("test_state");

    @Test
    void costEfficiencyScoresCheaperPlansHigher() {
        var cheap = plan("cheap", 2f, action("a"));
        var expensive = plan("expensive", 10f, action("a"));
        var allPlans = List.of(cheap, expensive);

        var scorer = PlanScorers.<Object>costEfficiency();
        var worldState = WorldState.create();

        assertEquals(0.8f, scorer.score(cheap, new Object(), worldState, allPlans), 0.0001f);
        assertEquals(0f, scorer.score(expensive, new Object(), worldState, allPlans), 0.0001f);
    }

    @Test
    void costEfficiencyScoresZeroCostPlansAtOne() {
        var plan = plan("zero", 0f, action("a"));
        var scorer = PlanScorers.<Object>costEfficiency();

        assertEquals(1f, scorer.score(plan, new Object(), WorldState.create(), List.of(plan)), 0.0001f);
    }

    @Test
    void goalDesirabilityDelegatesToSuppliedFunction() {
        var highPriority = plan("high", 1f, action("a"));
        var lowPriority = plan("low", 1f, action("a"));
        var scorer = PlanScorers.<Object>goalDesirability(
            (goal, actor, worldState) -> goal.getName().equals("high") ? 0.9f : 0.1f
        );

        assertEquals(
            0.9f,
            scorer.score(highPriority, new Object(), WorldState.create(), List.of(highPriority)),
            0.0001f
        );
        assertEquals(0.1f, scorer.score(lowPriority, new Object(), WorldState.create(), List.of(lowPriority)), 0.0001f);
    }

    @Test
    void brevityScoresShorterPlansHigher() {
        var shortPlan = plan("short", 1f, action("a"));
        var mediumPlan = plan("medium", 1f, action("a"), action("b"));
        var longPlan = plan("long", 1f, action("a"), action("b"), action("c"));
        var allPlans = List.of(shortPlan, mediumPlan, longPlan);

        var scorer = PlanScorers.<Object>brevity();
        var worldState = WorldState.create();

        assertEquals(1f, scorer.score(shortPlan, new Object(), worldState, allPlans), 0.0001f);
        assertEquals(0.5f, scorer.score(mediumPlan, new Object(), worldState, allPlans), 0.0001f);
        assertEquals(0f, scorer.score(longPlan, new Object(), worldState, allPlans), 0.0001f);
    }

    @Test
    void weightedAveragesScoresByWeight() {
        PlanScorer<Object> scorer = PlanScorers.weighted(
            new PlanScorers.WeightedScorer<>((plan, actor, worldState, allPlans) -> 1f, 2f),
            new PlanScorers.WeightedScorer<>((plan, actor, worldState, allPlans) -> 0.25f, 1f)
        );
        var plan = plan("weighted", 1f, action("a"));

        assertEquals(0.75f, scorer.score(plan, new Object(), WorldState.create(), List.of(plan)), 0.0001f);
    }

    @Test
    void weightedRejectsNegativeWeights() {
        PlanScorer<Object> scorer = (plan, actor, worldState, allPlans) -> 1f;

        assertThrows(IllegalArgumentException.class, () -> new PlanScorers.WeightedScorer<>(scorer, -1f));
    }

    @Test
    void multiplicativeZeroGatesPlans() {
        PlanScorer<Object> scorer = PlanScorers.multiplicative(
            (plan, actor, worldState, allPlans) -> 0.8f,
            (plan, actor, worldState, allPlans) -> 0f,
            (plan, actor, worldState, allPlans) -> 1f
        );
        var plan = plan("multiplicative", 1f, action("a"));

        assertEquals(0f, scorer.score(plan, new Object(), WorldState.create(), List.of(plan)), 0.0001f);
    }

    @SafeVarargs
    private static Plan<Object> plan(String goalName, float initialCost, Action<Object>... actions) {
        return new Plan<>(goal(goalName), List.of(actions), initialCost);
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
