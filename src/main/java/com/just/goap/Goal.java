package com.just.goap;

import com.just.goap.condition.Condition;
import com.just.goap.condition.ConditionContainer;
import com.just.goap.condition.expression.Expression;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Goal {

    public static <T> Goal of(String name, TypedIdentifier<? super T> identifier, Expression<? super T> expression) {
        return of(name, new Condition<T>(identifier, expression));
    }

    public static Goal of(String name, Condition<?> condition) {
        return builder(name, condition).build();
    }

    public static Builder builder(String name, Condition<?> condition) {
        return new Builder(name, condition);
    }

    private final ConditionContainer desiredConditions;

    private final String name;

    private Goal(ConditionContainer conditionContainer, String name) {
        this.desiredConditions = conditionContainer;
        this.name = name;
    }

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

    public static class Builder {

        private final List<Condition<?>> desiredConditions;

        private String name;

        private Builder(String name, Condition<?> condition) {
            this.desiredConditions = new ArrayList<>();
            this.name = name;

            desiredConditions.add(condition);
        }

        public Builder addDesiredCondition(Condition<?> condition) {
            desiredConditions.add(condition);
            return this;
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Goal build() {
            return new Goal(ConditionContainer.of(Collections.unmodifiableList(desiredConditions)), name);
        }
    }
}
