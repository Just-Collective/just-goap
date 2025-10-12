package com.just.goap.condition;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.just.goap.Satisfiable;
import com.just.goap.state.ReadableWorldState;

public class ConditionContainer implements Satisfiable {

    public static ConditionContainer of(Condition<?>... conditions) {
        return of(List.of(conditions));
    }

    public static ConditionContainer of(List<Condition<?>> conditions) {
        return new ConditionContainer(conditions);
    }

    private final List<Condition<?>> conditions;

    private ConditionContainer(List<Condition<?>> conditions) {
        this.conditions = conditions;
    }

    @Override
    public boolean satisfiedBy(ReadableWorldState worldState) {
        return conditions.stream().allMatch(condition -> condition.satisfiedBy(worldState));
    }

    public ConditionContainer filterUnsatisfied(ReadableWorldState worldState) {
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

    public boolean isEmpty() {
        return conditions.isEmpty();
    }

    public List<Condition<?>> getConditions() {
        return conditions;
    }
}
