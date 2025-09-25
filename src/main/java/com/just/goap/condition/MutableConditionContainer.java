package com.just.goap.condition;

import java.util.ArrayList;

public class MutableConditionContainer extends ConditionContainer {

    public MutableConditionContainer() {
        super(new ArrayList<>());
    }

    public void addCondition(Condition<?> condition) {
        conditions.add(condition);
    }
}
