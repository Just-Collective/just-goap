package com.just.goap.plan;

import com.just.core.functional.function.Lazy;
import com.just.core.functional.option.Option;
import com.just.goap.Action;
import com.just.goap.Goal;
import com.just.goap.state.Blackboard;
import com.just.goap.state.ReadableWorldState;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

public class Plan<T> {

    private final Goal goal;

    private final List<Action<T>> actions;

    private final Blackboard blackboard;

    private final Lazy<String> actionsString;

    private int currentActionIndex;

    public Plan(Goal goal, List<Action<T>> actions) {
        this.goal = goal;
        this.actions = actions;
        this.blackboard = new Blackboard();
        this.actionsString = Lazy.of(() -> actions.stream().map(Action::getName).collect(Collectors.joining(", ")));
        this.currentActionIndex = 0;
    }

    public State update(T context, ReadableWorldState currentState) {
        if (currentActionIndex >= actions.size()) {
            return State.FINISHED;
        }

        var action = actions.get(currentActionIndex);

        if (!action.getPreconditionContainer().satisfiedBy(currentState)) {
            action.onFinish(context, currentState, blackboard);
            return State.INVALID;
        }

        // If the world state already satisfies the effects of the action...
        if (currentState.satisfies(action.getEffectContainer())) {
            // Then the action can be considered complete, move on to the next action or finish.
            action.onFinish(context, currentState, blackboard);
            return proceedToNextActionOrFinish();
        }

        var performResult = action.perform(context, currentState, blackboard);

        return switch (performResult) {
            case CONTINUE -> State.IN_PROGRESS;
            case FAILED -> State.FAILED;
            case FINISHED -> {
                action.onFinish(context, currentState, blackboard);
                yield proceedToNextActionOrFinish();
            }
        };
    }

    private @NotNull State proceedToNextActionOrFinish() {
        // Move to the next action index.
        this.currentActionIndex += 1;
        // Evaluate based on current index if we've reached the end of the plan sequence or have more actions left.
        return currentActionIndex >= actions.size()
            ? State.FINISHED
            : State.IN_PROGRESS;
    }

    public Option<Action<T>> getCurrentAction() {
        return currentActionIndex >= actions.size()
            ? Option.none()
            : Option.some(actions.get(currentActionIndex));
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
        FAILED,
        FINISHED,
        IN_PROGRESS,
        INVALID;
    }
}
