package com.just.goap;

import com.just.goap.condition.Condition;
import com.just.goap.condition.ConditionContainer;
import com.just.goap.condition.expression.Expression;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Goal {

    public static Builder builder(String name) {
        return new Builder(name);
    }

    private final ConditionContainer desiredConditions;

    private final String name;

    private final ConditionContainer preconditions;

    private Goal(
        ConditionContainer desiredConditionsContainer,
        String name,
        ConditionContainer preconditionsContainer
    ) {
        this.desiredConditions = desiredConditionsContainer;
        this.name = name;
        this.preconditions = preconditionsContainer;
    }

    public ConditionContainer getDesiredConditions() {
        return desiredConditions;
    }

    public String getName() {
        return name;
    }

    public ConditionContainer getPreconditions() {
        return preconditions;
    }

    @Override
    public String toString() {
        return getName();
    }

    public static class Builder {

        private final List<Condition<?>> desiredConditions;

        private final List<Condition<?>> preconditions;

        private String name;

        private Builder(String name) {
            this.desiredConditions = new ArrayList<>();
            this.name = name;
            this.preconditions = new ArrayList<>();
        }

        public <U> Builder addDesiredCondition(GOAPKey<? super U> key, Expression<? super U> expression) {
            return addDesiredCondition(new Condition<>(key, expression));
        }

        public Builder addDesiredCondition(Condition<?> condition) {
            desiredConditions.add(condition);
            return this;
        }

        public <U> Builder addPrecondition(GOAPKey<? super U> key, Expression<? super U> expression) {
            return addPrecondition(new Condition<>(key, expression));
        }

        public Builder addPrecondition(Condition<?> condition) {
            preconditions.add(condition);
            return this;
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Goal build() {
            if (desiredConditions.isEmpty()) {
                throw new IllegalStateException("Goal must have at least one desired condition.");
            }

            return new Goal(
                ConditionContainer.of(Collections.unmodifiableList(desiredConditions)),
                name,
                ConditionContainer.of(Collections.unmodifiableList(preconditions))
            );
        }
    }
}
