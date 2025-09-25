package com.just.goap.condition;

import com.just.goap.state.WorldState;

import java.util.Arrays;
import java.util.List;

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

}
