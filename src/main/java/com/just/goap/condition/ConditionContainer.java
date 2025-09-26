package com.just.goap.condition;

import com.just.goap.state.WorldState;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ConditionContainer {

    public static ConditionContainer of(Condition<?>... conditions) {
        return new ConditionContainer(Arrays.stream(conditions).toList());
    }

    protected final List<Condition<?>> conditions;

    protected ConditionContainer(List<Condition<?>> conditions) {
        this.conditions = conditions;
    }

    public List<Condition<?>> getConditions() {
        return conditions;
    }

    public boolean satisfiedBy(WorldState worldState) {
        return conditions.stream().allMatch(condition -> condition.satisfiedBy(worldState));
    }

    public ConditionContainer filterUnsatisfied(WorldState worldState) {
        var unsatisfied = conditions.stream()
            .filter(condition -> !condition.satisfiedBy(worldState))
            .collect(Collectors.toList());

        return new ConditionContainer(unsatisfied);
    }

    public ConditionContainer without(Condition<?>... toRemove) {
        var removeSet = Set.of(toRemove);
        var remaining = conditions.stream()
            .filter(cond -> !removeSet.contains(cond))
            .toList();

        return new ConditionContainer(remaining);
    }

    public ConditionContainer union(ConditionContainer other) {
        var combined = new LinkedHashSet<>(conditions);
        combined.addAll(other.conditions);
        return new ConditionContainer(new ArrayList<>(combined));
    }

}
