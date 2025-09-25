package com.just.goap;

import com.just.goap.condition.ConditionContainer;

public abstract class Goal {

    private final ConditionContainer desiredConditions;

    private final String name;

    protected Goal() {
        this.desiredConditions = createDesiredConditions();
        this.name = this.getClass().getSimpleName();
    }

    protected abstract ConditionContainer createDesiredConditions();

    public ConditionContainer getDesiredConditions() {
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
