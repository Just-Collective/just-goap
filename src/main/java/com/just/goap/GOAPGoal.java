package com.just.goap;

import com.just.goap.condition.GOAPConditionContainer;

public abstract class GOAPGoal {

    private final GOAPConditionContainer desiredConditions;

    private final String name;

    protected GOAPGoal() {
        this.desiredConditions = createDesiredConditions();
        this.name = this.getClass().getSimpleName();
    }

    protected abstract GOAPConditionContainer createDesiredConditions();

    public GOAPConditionContainer getDesiredConditions() {
        return desiredConditions;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return getName();
    }
}
