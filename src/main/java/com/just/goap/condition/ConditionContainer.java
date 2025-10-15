package com.just.goap.condition;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

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
        for (Condition<?> condition : conditions) {
            if (!condition.satisfiedBy(worldState)) {
                return false;
            }
        }

        return true;
    }

    public ConditionContainer filterUnsatisfied(ReadableWorldState worldState) {
        var unsatisfied = new ArrayList<Condition<?>>();

        for (var condition : conditions) {
            if (!condition.satisfiedBy(worldState)) {
                unsatisfied.add(condition);
            }
        }

        return new ConditionContainer(unsatisfied);
    }

    public ConditionContainer without(Condition<?>... toRemove) {
        var removeSet = Set.of(toRemove);
        var remaining = new ArrayList<Condition<?>>();

        for (var condition : conditions) {
            if (!removeSet.contains(condition)) {
                remaining.add(condition);
            }
        }

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
