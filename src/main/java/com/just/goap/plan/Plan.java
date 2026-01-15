package com.just.goap.plan;

import java.util.List;
import java.util.stream.Collectors;

import com.just.core.functional.function.Lazy;
import com.just.goap.Agent;
import com.just.goap.Goal;
import com.just.goap.action.Action;
import com.just.goap.state.Blackboard;
import com.just.goap.state.ReadableWorldState;

public class Plan<T> {

    private final Goal goal;

    private final List<Action<? super T>> actions;

    private final Blackboard actionBlackboard;

    private final Action.Context<T> actionContext;

    private final Lazy<String> actionsString;

    private final Blackboard blackboard;

    private int currentActionIndex;

    private int actionTick;

    private int tick;

    public Plan(Goal goal, List<Action<? super T>> actions) {
        this.goal = goal;
        this.actions = actions;
        this.actionBlackboard = new Blackboard();
        this.actionContext = new Action.Context<>();
        this.actionsString = Lazy.of(() -> actions.stream().map(Action::getName).collect(Collectors.joining(", ")));
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

    public int getActionTick() {
        return actionTick;
    }

    public Blackboard getBlackboard() {
        return blackboard;
    }

    public State getPlanState() {
        return currentActionIndex >= actions.size()
            ? State.FINISHED
            : State.IN_PROGRESS;
    }

    public int getTick() {
        return tick;
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
