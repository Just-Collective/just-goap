package com.just.goap;

import com.just.core.functional.option.Option;
import com.just.goap.plan.GOAPPlan;
import com.just.goap.plan.GOAPPlanner;
import com.just.goap.state.GOAPMutableWorldState;
import com.just.goap.state.GOAPWorldState;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class GOAP<T> {

    private final Set<GOAPAction<T>> actions;

    private final Set<GOAPGoal> goals;

    private final GOAPPlanner<T> planner;

    private final List<GOAPSensor<? super T>> sensors;

    private @NotNull Option<GOAPPlan<T>> currentPlanOption;

    private boolean isEnabled;

    protected GOAP() {
        this.planner = new GOAPPlanner<>(this);
        this.actions = new HashSet<>();
        this.goals = new HashSet<>();
        this.sensors = new ArrayList<>();
        this.currentPlanOption = Option.none();
        this.isEnabled = true;
    }

    public void addAction(GOAPAction<T> action) {
        actions.add(action);
    }

    public void addGoal(GOAPGoal goal) {
        goals.add(goal);
    }

    public void addSensor(GOAPSensor<? super T> sensor) {
        sensors.add(sensor);
    }

    public GOAPWorldState sense(T context) {
        var state = new GOAPMutableWorldState();

        for (var sensor : sensors) {
            sensor.sense(context, state);
        }

        return state;
    }

    public void update(T context) {
        if (!isEnabled) {
            return;
        }

        // Sense all world input that we need to.
        var worldState = sense(context);

        if (currentPlanOption.isNone()) {
            this.currentPlanOption = planner.createPlan(context, worldState, goals);
        }

        currentPlanOption.ifSome(plan -> {
            var planState = plan.update(context, worldState);

            switch (planState) {
                case GOAPPlan.State.Failed failed -> this.currentPlanOption = Option.none();
                case GOAPPlan.State.Finished finished -> this.currentPlanOption = Option.none();
                case GOAPPlan.State.Invalid invalid -> this.currentPlanOption = Option.none();
                case GOAPPlan.State.InProgress inProgress -> {}
            }
        });
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }

    public Collection<GOAPAction<T>> getAvailableActions() {
        return actions;
    }
}
