package com.just.goap.action;

import java.util.function.UnaryOperator;

import com.just.goap.StateKey;
import com.just.goap.condition.Condition;
import com.just.goap.condition.ConditionContainer;
import com.just.goap.condition.expression.Expression;
import com.just.goap.effect.Effect;
import com.just.goap.effect.EffectContainer;
import com.just.goap.state.ReadableWorldState;

/**
 * An abstract action that delegates all {@link Action} methods to a wrapped delegate.
 * <p>
 * Extend this class to create custom action wrappers that add additional data or behavior without re-implementing the
 * delegation boilerplate.
 * <p>
 * Example:
 *
 * <pre>{@code
 * public class PriorityAction<T> extends DelegatingAction<T> {
 *
 *     private final int priority;
 *
 *     public PriorityAction(Action<T> delegate, int priority) {
 *         super(delegate);
 *         this.priority = priority;
 *     }
 *
 *     public int getPriority() {
 *         return priority;
 *     }
 * }
 * }</pre>
 *
 * @param <T> The actor type.
 */
public abstract class DelegatingAction<T> implements Action<T> {

    private final Action<T> delegate;

    protected DelegatingAction(Action<T> delegate) {
        this.delegate = delegate;
    }

    /**
     * Returns the wrapped delegate action.
     */
    public Action<T> getDelegate() {
        return delegate;
    }

    @Override
    public float getCost(T actor, ReadableWorldState worldState) {
        return delegate.getCost(actor, worldState);
    }

    @Override
    public Signal perform(Context<? extends T> context) {
        return delegate.perform(context);
    }

    @Override
    public void onStart(Context<? extends T> context) {
        delegate.onStart(context);
    }

    @Override
    public void onFinish(Context<? extends T> context) {
        delegate.onFinish(context);
    }

    @Override
    public ConditionContainer getPreconditionContainer() {
        return delegate.getPreconditionContainer();
    }

    @Override
    public EffectContainer getEffectContainer() {
        return delegate.getEffectContainer();
    }

    @Override
    public String getName() {
        return delegate.getName();
    }

    @Override
    public String toString() {
        return delegate.toString();
    }

    /**
     * Abstract builder base for {@link DelegatingAction} subclasses.
     * <p>
     * Composes with {@link BaseAction.ConcreteBuilder} to provide all standard action building methods. Subclasses add
     * their own custom builder methods and implement {@link #build(Action)} to construct the final wrapper.
     * <p>
     * Example:
     *
     * <pre>
     *
     * {
     *     &#64;code
     *     public class PriorityAction<T> extends DelegatingAction<T> {
     *
     *         public static <T> ConcreteBuilder<T> builder(String name) {
     *             return new ConcreteBuilder<>(name);
     *         }
     *
     *         // ... fields and constructor ...
     *
     *         public abstract static class Builder<T, B extends Builder<T, B>> extends DelegatingAction.Builder<T, B> {
     *
     *             protected int priority = 0;
     *
     *             protected Builder(String name) {
     *                 super(name);
     *             }
     *
     *             public B withPriority(int priority) {
     *                 this.priority = priority;
     *                 return self();
     *             }
     *
     *             &#64;Override
     *             protected PriorityAction<T> build(Action<T> delegate) {
     *                 return new PriorityAction<>(delegate, priority);
     *             }
     *         }
     *
     *         public static class ConcreteBuilder<T> extends Builder<T, ConcreteBuilder<T>> {
     *
     *             protected ConcreteBuilder(String name) {
     *                 super(name);
     *             }
     *
     *             @Override
     *             protected ConcreteBuilder<T> self() {
     *                 return this;
     *             }
     *         }
     *     }
     * }
     * </pre>
     *
     * @param <T> The actor type.
     * @param <B> The self-type for fluent chaining.
     */
    public abstract static class Builder<T, B extends Builder<T, B>> {

        protected final BaseAction.ConcreteBuilder<T> baseBuilder;

        protected Builder(String name) {
            this.baseBuilder = BaseAction.builder(name);
        }

        protected abstract B self();

        /**
         * Builds the final delegating action wrapper around the given delegate. Subclasses implement this to construct
         * their specific wrapper type.
         *
         * @param delegate The base action to wrap.
         * @return The wrapped action.
         */
        protected abstract DelegatingAction<T> build(Action<T> delegate);

        // Delegated BaseAction.Builder methods

        public <U> B addPrecondition(StateKey.Derived<? extends U> key, Expression<? super U> expression) {
            baseBuilder.addPrecondition(key, expression);
            return self();
        }

        public <U> B addPrecondition(StateKey.Sensed<? extends U> key, Expression<? super U> expression) {
            baseBuilder.addPrecondition(key, expression);
            return self();
        }

        public B addPrecondition(Condition<?> condition) {
            baseBuilder.addPrecondition(condition);
            return self();
        }

        public <U> B addEffect(StateKey.Derived<U> key, UnaryOperator<U> consumer) {
            baseBuilder.addEffect(key, consumer);
            return self();
        }

        public <U> B addEffect(StateKey.Derived<U> key, U value) {
            baseBuilder.addEffect(key, value);
            return self();
        }

        public B addEffect(Effect<?> effect) {
            baseBuilder.addEffect(effect);
            return self();
        }

        public B withName(String name) {
            baseBuilder.withName(name);
            return self();
        }

        public B withCostCallback(Action.CostCallback<T> costCallback) {
            baseBuilder.withCostCallback(costCallback);
            return self();
        }

        public B withStartCallback(Action.StartCallback<T> startCallback) {
            baseBuilder.withStartCallback(startCallback);
            return self();
        }

        public B withPerformCallback(Action.PerformCallback<T> performCallback) {
            baseBuilder.withPerformCallback(performCallback);
            return self();
        }

        public B withFinishCallback(Action.FinishCallback<T> finishCallback) {
            baseBuilder.withFinishCallback(finishCallback);
            return self();
        }

        public B apply(UnaryOperator<B> unaryOperator) {
            return unaryOperator.apply(self());
        }

        /**
         * Builds the base action and wraps it using {@link #build(Action)}.
         *
         * @return The final wrapped action.
         */
        public DelegatingAction<T> build() {
            var baseAction = baseBuilder.build();
            return build(baseAction);
        }
    }
}
