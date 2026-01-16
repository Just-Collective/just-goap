package com.just.goap.goal;

import java.util.function.UnaryOperator;

import com.just.goap.StateKey;
import com.just.goap.condition.Condition;
import com.just.goap.condition.ConditionContainer;
import com.just.goap.condition.expression.Expression;

/**
 * An abstract goal that delegates all {@link Goal} methods to a wrapped delegate.
 * <p>
 * Extend this class to create custom goal wrappers that add additional data or behavior without re-implementing the
 * delegation boilerplate.
 * <p>
 * Example:
 *
 * <pre>{@code
 * public class PriorityGoal extends DelegatingGoal {
 *
 *     private final int priority;
 *
 *     public PriorityGoal(Goal delegate, int priority) {
 *         super(delegate);
 *         this.priority = priority;
 *     }
 *
 *     public int getPriority() {
 *         return priority;
 *     }
 * }
 * }</pre>
 */
public abstract class DelegatingGoal implements Goal {

    private final Goal delegate;

    protected DelegatingGoal(Goal delegate) {
        this.delegate = delegate;
    }

    /**
     * Returns the wrapped delegate goal.
     */
    public Goal getDelegate() {
        return delegate;
    }

    @Override
    public ConditionContainer getDesiredConditions() {
        return delegate.getDesiredConditions();
    }

    @Override
    public String getName() {
        return delegate.getName();
    }

    @Override
    public ConditionContainer getPreconditions() {
        return delegate.getPreconditions();
    }

    @Override
    public String toString() {
        return delegate.toString();
    }

    /**
     * Abstract builder base for {@link DelegatingGoal} subclasses.
     * <p>
     * Composes with {@link BaseGoal.ConcreteBuilder} to provide all standard goal building methods. Subclasses add
     * their own custom builder methods and implement {@link #build(Goal)} to construct the final wrapper.
     *
     * @param <B> The self-type for fluent chaining.
     */
    public abstract static class Builder<B extends Builder<B>> {

        protected final BaseGoal.ConcreteBuilder baseBuilder;

        protected Builder(String name) {
            this.baseBuilder = BaseGoal.builder(name);
        }

        protected abstract B self();

        /**
         * Builds the final delegating goal wrapper around the given delegate. Subclasses implement this to construct
         * their specific wrapper type.
         *
         * @param delegate The base goal to wrap.
         * @return The wrapped goal.
         */
        protected abstract DelegatingGoal build(Goal delegate);

        // Delegated BaseGoal.Builder methods

        public <U> B addDesiredCondition(StateKey.Derived<? extends U> key, Expression<? super U> expression) {
            baseBuilder.addDesiredCondition(key, expression);
            return self();
        }

        public <U> B addDesiredCondition(Condition.Derived<U> condition) {
            baseBuilder.addDesiredCondition(condition);
            return self();
        }

        public <U> B addPrecondition(StateKey.Sensed<? extends U> key, Expression<? super U> expression) {
            baseBuilder.addPrecondition(key, expression);
            return self();
        }

        public <U> B addPrecondition(Condition.Sensed<U> condition) {
            baseBuilder.addPrecondition(condition);
            return self();
        }

        public B withName(String name) {
            baseBuilder.withName(name);
            return self();
        }

        public B apply(UnaryOperator<B> unaryOperator) {
            return unaryOperator.apply(self());
        }

        /**
         * Builds the base goal and wraps it using {@link #build(Goal)}.
         *
         * @return The final wrapped goal.
         */
        public DelegatingGoal build() {
            var baseGoal = baseBuilder.build();
            return build(baseGoal);
        }
    }
}
