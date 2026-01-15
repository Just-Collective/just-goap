package com.just.goap.plan;

import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BestPlanExecutor<T> implements PlanExecutor<T> {

    private @Nullable Plan<T> currentPlan;

    public BestPlanExecutor() {
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
    public void supplyPlans(List<Plan<T>> plans) {
        if (!plans.isEmpty() && currentPlan == null) {
            // Select the best (first) plan
            this.currentPlan = plans.getFirst();
        }
    }

    @Override
    public boolean needsPlans() {
        return currentPlan == null;
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
}
