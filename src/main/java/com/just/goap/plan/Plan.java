package com.just.goap.plan;

import java.util.List;
import java.util.stream.Collectors;

import com.just.core.functional.function.Lazy;
import com.just.goap.Action;
import com.just.goap.Agent;
import com.just.goap.Goal;
import com.just.goap.state.Blackboard;
import com.just.goap.state.ReadableWorldState;

public class Plan<T> {

    private final Goal goal;

    private final List<Action<? super T>> actions;

    private final Blackboard blackboard;

    private final Action.Context<T> actionContext;

    private final Lazy<String> actionsString;

    private int currentActionIndex;

    private int currentActionTick;

    public Plan(Goal goal, List<Action<? super T>> actions) {
        this.goal = goal;
        this.actions = actions;
        this.blackboard = new Blackboard();
        this.actionContext = new Action.Context<>();
        this.actionsString = Lazy.of(() -> actions.stream().map(Action::getName).collect(Collectors.joining(", ")));
        this.currentActionIndex = 0;
        this.currentActionTick = 0;
    }

    @SuppressWarnings("unchecked")
    public State update(Agent<T> agent, T context, ReadableWorldState currentState, ReadableWorldState previousState) {
        if (getPlanState() == State.FINISHED) {
            return State.FINISHED;
        }

        // Update the reusable action context with current values.
        actionContext.set(agent, context, currentState, previousState, blackboard);

        var currentAction = (Action<T>) actions.get(currentActionIndex);

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

        if (currentActionTick == 0) {
            // Trigger onStart callback for the action if the current tick is the first tick.
            currentAction.onStart(actionContext);
        }

        // Run the action.
        var signal = currentAction.perform(actionContext);

        // Increment the current action tick after performing the action.
        currentActionTick++;

        return switch (signal) {
            case ABORT -> State.ABORTED;
            case CONTINUE -> State.IN_PROGRESS;
        };
    }

    private void proceedToNextAction() {
        // Move to the next action index.
        this.currentActionIndex = Math.clamp(this.currentActionIndex + 1, 0, actions.size());
        // Reset the current action tick to 0.
        this.currentActionTick = 0;
    }

    private State getPlanState() {
        return currentActionIndex >= actions.size()
            ? State.FINISHED
            : State.IN_PROGRESS;
    }

    @Override
    public String toString() {
        return "Plan{" +
            "currentActionIndex=" + currentActionIndex +
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
