package com.just.goap;

import com.just.goap.condition.Condition;
import com.just.goap.condition.ConditionContainer;
import com.just.goap.condition.MutableConditionContainer;
import com.just.goap.condition.expression.Expression;
import com.just.goap.effect.Effect;
import com.just.goap.effect.EffectContainer;
import com.just.goap.effect.MutableEffectContainer;
import com.just.goap.state.Blackboard;
import com.just.goap.state.WorldState;

public abstract class Action<T> {

    private final MutableEffectContainer effects;

    private final String name;

    private final MutableConditionContainer preconditions;

    public Action() {
        this.effects = new MutableEffectContainer();
        this.name = this.getClass().getSimpleName();
        this.preconditions = new MutableConditionContainer();
    }

    protected final <U> void addPrecondition(
        TypedIdentifier<? extends U> identifier,
        Expression<? super U> condition
    ) {
        preconditions.addCondition(new Condition<>(identifier, condition));
    }

    protected final void addEffect(Effect<?> effect) {
        effects.addEffect(effect);
    }

    public abstract boolean perform(T context, WorldState worldState, Blackboard blackboard);

    public void onFinish(T context, WorldState worldState, Blackboard blackboard) {}

    public float getCost(T context, WorldState worldState) {
        return 0.0F;
    }

    public EffectContainer getEffectContainer() {
        return effects;
    }

    public String getName() {
        return name;
    }

    public ConditionContainer getPreconditions() {
        return preconditions;
    }
}
