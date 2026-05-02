package com.just.ai.goap.plan.scorer;

import java.util.List;

public final class PlanScorers {

    private PlanScorers() {}

    public static <T> PlanScorer<T> costEfficiency() {
        return (plan, actor, worldState, allPlans) -> {
            var maxCost = 0f;

            for (var otherPlan : allPlans) {
                maxCost = Math.max(maxCost, otherPlan.getInitialCost());
            }

            if (maxCost == 0f) {
                return 1f;
            }

            return clamp01(1f - (plan.getInitialCost() / maxCost));
        };
    }

    public static <T> PlanScorer<T> goalDesirability(GoalDesirability<T> desirability) {
        return (plan, actor, worldState, allPlans) -> clamp01(
            desirability.getDesirability(plan.getGoal(), actor, worldState)
        );
    }

    public static <T> PlanScorer<T> brevity() {
        return (plan, actor, worldState, allPlans) -> {
            var maxActions = 0;

            for (var otherPlan : allPlans) {
                maxActions = Math.max(maxActions, otherPlan.getActions().size());
            }

            if (maxActions <= 1) {
                return 1f;
            }

            return clamp01(1f - ((float) (plan.getActions().size() - 1) / (maxActions - 1)));
        };
    }

    @SafeVarargs
    public static <T> PlanScorer<T> weighted(WeightedScorer<T>... weightedScorers) {
        var scorers = List.of(weightedScorers);

        return (plan, actor, worldState, allPlans) -> {
            var totalWeight = 0f;
            var totalScore = 0f;

            for (var weightedScorer : scorers) {
                totalWeight += weightedScorer.weight();
                totalScore += weightedScorer.weight()
                    * clamp01(weightedScorer.scorer().score(plan, actor, worldState, allPlans));
            }

            return totalWeight == 0f ? 0f : clamp01(totalScore / totalWeight);
        };
    }

    @SafeVarargs
    public static <T> PlanScorer<T> multiplicative(PlanScorer<T>... planScorers) {
        var scorers = List.of(planScorers);

        return (plan, actor, worldState, allPlans) -> {
            var result = 1f;

            for (var scorer : scorers) {
                result *= clamp01(scorer.score(plan, actor, worldState, allPlans));

                if (result == 0f) {
                    return 0f;
                }
            }

            return clamp01(result);
        };
    }

    public record WeightedScorer<T>(
        PlanScorer<T> scorer,
        float weight
    ) {

        public WeightedScorer {
            if (weight < 0f) {
                throw new IllegalArgumentException("weight must not be negative");
            }
        }
    }

    private static float clamp01(float score) {
        return Math.clamp(score, 0f, 1f);
    }
}
