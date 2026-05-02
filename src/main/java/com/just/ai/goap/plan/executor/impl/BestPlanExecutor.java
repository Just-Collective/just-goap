package com.just.ai.goap.plan.executor.impl;

import org.jetbrains.annotations.Nullable;

import java.util.List;

import com.just.ai.goap.plan.Plan;
import com.just.ai.goap.plan.executor.PlanExecutor;
import com.just.ai.goap.plan.scorer.PlanScorer;
import com.just.ai.goap.plan.scorer.PlanScorers;
import com.just.ai.goap.state.ReadableWorldState;

public class BestPlanExecutor<T> implements PlanExecutor<T> {

    private final PlanScorer<T> planScorer;

    private @Nullable Plan<T> currentPlan;

    public BestPlanExecutor() {
        this(PlanScorers.costEfficiency());
    }

    public BestPlanExecutor(PlanScorer<T> planScorer) {
        this.planScorer = planScorer;
        this.currentPlan = null;
    }

    @Override
    public Result execute(ExecutionContext<T> context) {
        if (currentPlan == null) {
            return Result.NO_PLANS;
        }

        var debugger = context.agent().getDebugger();
        debugger.push("BestPlanExecutor.execute()");

        var planState = currentPlan.update(
            context.agent(),
            context.actor(),
            context.currentWorldState(),
            context.previousWorldState()
        );

        debugger.pop();

        return switch (planState) {
            case ABORTED, FINISHED, INVALID -> {
                currentPlan = null;
                yield Result.IDLE;
            }
            case IN_PROGRESS -> Result.IN_PROGRESS;
        };
    }

    @Override
    public void supplyPlans(List<Plan<T>> plans, T actor, ReadableWorldState worldState) {
        if (!plans.isEmpty() && currentPlan == null) {
            this.currentPlan = selectBestPlan(plans, actor, worldState);
        }
    }

    @Override
    public boolean hasActivePlans() {
        return currentPlan != null;
    }

    @Override
    public void abandonAllPlans() {
        this.currentPlan = null;
    }

    /**
     * Returns the currently executing plan, if any.
     */
    public @Nullable Plan<T> getCurrentPlan() {
        return currentPlan;
    }

    private Plan<T> selectBestPlan(List<Plan<T>> plans, T actor, ReadableWorldState worldState) {
        Plan<T> bestPlan = null;
        var bestScore = -1f;

        for (var plan : plans) {
            var score = planScorer.score(plan, actor, worldState, plans);

            if (score > bestScore) {
                bestScore = score;
                bestPlan = plan;
            }
        }

        return bestPlan == null ? plans.getFirst() : bestPlan;
    }
}
