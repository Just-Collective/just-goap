package com.just.goap;

import com.just.goap.condition.Condition;
import com.just.goap.condition.ConditionContainer;
import com.just.goap.condition.expression.Expression;

import java.util.ArrayList;
import java.util.List;

public final class Goal {

    public static <T> Goal of(TypedIdentifier<? super T> identifier, Expression<? super T> expression) {
        return of(new Condition<T>(identifier, expression));
    }

    public static Goal of(Condition<?> condition) {
        return builder(condition).build();
    }

    public static Builder builder(Condition<?> condition) {
        return new Builder(condition);
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

        private Builder(Condition<?> condition) {
            this.desiredConditions = new ArrayList<>();
            this.name = this.getClass().getSimpleName();

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
            return new Goal(ConditionContainer.of(desiredConditions), name);
        }
    }
}
