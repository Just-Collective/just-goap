package com.just.goap.goal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.UnaryOperator;

import com.just.goap.StateKey;
import com.just.goap.condition.Condition;
import com.just.goap.condition.ConditionContainer;
import com.just.goap.condition.expression.Expression;

/**
 * The standard implementation of {@link Goal}.
 */
public class BaseGoal implements Goal {

    public static ConcreteBuilder builder(String name) {
        return new ConcreteBuilder(name);
    }

    private final ConditionContainer desiredConditions;

    private final String name;

    private final ConditionContainer preconditions;

    protected BaseGoal(
        String name,
        ConditionContainer desiredConditions,
        ConditionContainer preconditions
    ) {
        this.name = name;
        this.desiredConditions = desiredConditions;
        this.preconditions = preconditions;
    }

    @Override
    public ConditionContainer getDesiredConditions() {
        return desiredConditions;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public ConditionContainer getPreconditions() {
        return preconditions;
    }

    @Override
    public String toString() {
        return getName();
    }

    /**
     * Abstract builder base for {@link BaseGoal} and subclasses.
     *
     * @param <B> The self-type for fluent chaining.
     */
    public abstract static class Builder<B extends Builder<B>> {

        protected final List<Condition.Derived<?>> desiredConditions;

        protected final List<Condition.Sensed<?>> preconditions;

        protected String name;

        protected Builder(String name) {
            this.desiredConditions = new ArrayList<>();
            this.preconditions = new ArrayList<>();
            this.name = name;
        }

        protected abstract B self();

        public <U> B addDesiredCondition(StateKey.Derived<? extends U> key, Expression<? super U> expression) {
            return addDesiredCondition(Condition.derived(key, expression));
        }

        public <U> B addDesiredCondition(Condition.Derived<U> condition) {
            desiredConditions.add(condition);
            return self();
        }

        public <U> B addPrecondition(StateKey.Sensed<? extends U> key, Expression<? super U> expression) {
            return addPrecondition(Condition.sensed(key, expression));
        }

        public <U> B addPrecondition(Condition.Sensed<U> condition) {
            preconditions.add(condition);
            return self();
        }

        public B withName(String name) {
            this.name = name;
            return self();
        }

        public B apply(UnaryOperator<B> unaryOperator) {
            return unaryOperator.apply(self());
        }

        public BaseGoal build() {
            if (desiredConditions.isEmpty()) {
                throw new IllegalStateException("Goal must have at least one desired condition.");
            }

            return new BaseGoal(
                name,
                ConditionContainer.of(Collections.unmodifiableList(desiredConditions)),
                ConditionContainer.of(Collections.unmodifiableList(preconditions))
            );
        }
    }

    /**
     * Concrete terminal builder for {@link BaseGoal}.
     */
    public static class ConcreteBuilder extends Builder<ConcreteBuilder> {

        protected ConcreteBuilder(String name) {
            super(name);
        }

        @Override
        protected ConcreteBuilder self() {
            return this;
        }
    }
}
