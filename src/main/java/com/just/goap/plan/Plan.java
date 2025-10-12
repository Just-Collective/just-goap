package com.just.goap.plan;

import java.util.List;
import java.util.stream.Collectors;

import com.just.core.functional.function.Lazy;
import com.just.goap.Action;
import com.just.goap.Goal;
import com.just.goap.state.Blackboard;
import com.just.goap.state.ReadableWorldState;

public class Plan<T> {

    private final Goal goal;

    private final List<Action<T>> actions;

    private final Blackboard blackboard;

    private final Lazy<String> actionsString;

    private int currentActionIndex;

    private int currentActionTick;

    public Plan(Goal goal, List<Action<T>> actions) {
        this.goal = goal;
        this.actions = actions;
        this.blackboard = new Blackboard();
        this.actionsString = Lazy.of(() -> actions.stream().map(Action::getName).collect(Collectors.joining(", ")));
        this.currentActionIndex = 0;
        this.currentActionTick = 0;
    }

    public State update(T context, ReadableWorldState currentState) {
        if (getPlanState() == State.FINISHED) {
            return State.FINISHED;
        }

        var currentAction = actions.get(currentActionIndex);

        // If the world state already satisfies the effects of the action...
        if (currentState.satisfies(currentAction.getEffectContainer())) {
            // Then the action can be considered complete, move on to the next action or finish.
            currentAction.onFinish(context, currentState, blackboard);
            // Move current action index forward.
            proceedToNextAction();

            var planState = getPlanState();

            if (planState == State.FINISHED) {
                return planState;
            }

            // Re-assign the current action reference before we run the next action.
            currentAction = actions.get(currentActionIndex);
        }

        // Always check the current action's preconditions before running the action.
        if (!currentAction.getPreconditionContainer().satisfiedBy(currentState)) {
            currentAction.onFinish(context, currentState, blackboard);
            return State.INVALID;
        }

        if (currentActionTick == 0) {
            // Trigger onStart callback for the action if the current tick is the first tick.
            currentAction.onStart(context, currentState, blackboard);
        }

        // Run the action.
        var signal = currentAction.perform(context, currentState, blackboard);

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
