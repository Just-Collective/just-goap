package com.just.goap.plan;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.just.core.functional.function.Lazy;
import com.just.goap.Agent;
import com.just.goap.action.Action;
import com.just.goap.goal.Goal;
import com.just.goap.state.Blackboard;
import com.just.goap.state.ReadableWorldState;

public class Plan<T> {

    private final Goal goal;

    private final List<Action<? super T>> actions;

    private final float initialCost;

    private final Blackboard actionBlackboard;

    private final Action.Context<T> actionContext;

    private final Lazy<String> actionsString;

    private final Blackboard blackboard;

    private int currentActionIndex;

    private int actionTick;

    private int tick;

    public Plan(Goal goal, List<Action<? super T>> actions, float initialCost) {
        this.goal = goal;
        this.actions = Collections.unmodifiableList(actions);
        this.initialCost = initialCost;
        this.actionBlackboard = new Blackboard();
        this.actionContext = new Action.Context<>();
        this.actionsString = Lazy.of(
            () -> actions.stream()
                .map(Action::getName)
                .collect(Collectors.joining(", "))
        );
        this.blackboard = new Blackboard();
        this.currentActionIndex = 0;
        this.actionTick = 0;
        this.tick = 0;
    }

    public State update(Agent<T> agent, T actor, ReadableWorldState currentState, ReadableWorldState previousState) {
        var state = updatePlan(agent, actor, currentState, previousState);
        tick++;
        return state;
    }

    public List<Action<? super T>> getActions() {
        return actions;
    }

    public int getActionTick() {
        return actionTick;
    }

    public Blackboard getBlackboard() {
        return blackboard;
    }

    public Goal getGoal() {
        return goal;
    }

    public float getInitialCost() {
        return initialCost;
    }

    public State getPlanState() {
        return currentActionIndex >= actions.size()
            ? State.FINISHED
            : State.IN_PROGRESS;
    }

    /**
     * Calculates the cost of the remaining actions in this plan based on the current world state.
     * <p>
     * This is useful for comparing plans during execution, as the original cost may be stale.
     *
     * @param actor      The actor executing the plan.
     * @param worldState The current world state.
     * @return The sum of costs for all remaining actions (including the current action).
     */
    public float getRemainingCost(T actor, ReadableWorldState worldState) {
        var remainingCost = 0.0f;

        for (var i = currentActionIndex; i < actions.size(); i++) {
            var action = actions.get(i);
            remainingCost += action.getCost(actor, worldState);
        }

        return remainingCost;
    }

    public int getTick() {
        return tick;
    }

    @SuppressWarnings("unchecked")
    private State updatePlan(
        Agent<T> agent,
        T actor,
        ReadableWorldState currentState,
        ReadableWorldState previousState
    ) {
        if (getPlanState() == State.FINISHED) {
            return State.FINISHED;
        }

        var currentAction = (Action<T>) actions.get(currentActionIndex);

        // Update the reusable action context with current values.
        actionContext.set(currentAction, actor, agent, actionBlackboard, this, currentState, previousState);

        // If the world state already satisfies the effects of the action...
        if (currentState.satisfies(currentAction.getEffectContainer())) {
            // Then the action can be considered complete, move on to the next action or finish.
            currentAction.onFinish(actionContext);
            // Move current action index forward.
            proceedToNextAction();

            var planState = getPlanState();

            if (planState == State.FINISHED) {
                return planState;
            }

            // Re-assign the current action reference before we run the next action.
            currentAction = (Action<T>) actions.get(currentActionIndex);
        }

        // Always check the current action's preconditions before running the action.
        if (!currentAction.getPreconditionContainer().satisfiedBy(currentState)) {
            currentAction.onFinish(actionContext);
            return State.INVALID;
        }

        var debugger = agent.getDebugger();

        if (actionTick == 0) {
            // Trigger onStart callback for the action if the current tick is the first tick.
            debugger.push("Action '" + currentAction.getName() + "' onStart()");
            currentAction.onStart(actionContext);
            debugger.pop();
        }

        // Run the action.
        debugger.push("Action '" + currentAction.getName() + "' perform()");
        var signal = currentAction.perform(actionContext);
        debugger.pop();

        // Increment the current action tick after performing the action.
        actionTick++;

        return switch (signal) {
            case ABORT -> State.ABORTED;
            case CONTINUE -> State.IN_PROGRESS;
        };
    }

    private void proceedToNextAction() {
        // Move to the next action index.
        this.currentActionIndex = Math.clamp(this.currentActionIndex + 1, 0, actions.size());
        // Reset the current action tick to 0.
        this.actionTick = 0;
        // Clear the action blackboard.
        actionBlackboard.clear();
    }

    @Override
    public String toString() {
        return "Plan{" +
            "tick=" + tick +
            ", cost=" + initialCost +
            ", currentActionIndex=" + currentActionIndex +
            ", actionTick=" + actionTick +
            ", actionBlackboard=" + actionBlackboard +
            ", blackboard=" + blackboard +
            ", actions=[" + actionsString.get() +
            "], goal=" + goal +
            '}';
    }

    public enum State {
        ABORTED,
        FINISHED,
        IN_PROGRESS,
        INVALID
    }
}
