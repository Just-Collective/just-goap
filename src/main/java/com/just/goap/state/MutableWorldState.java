package com.just.goap.state;

import com.just.goap.TypedIdentifier;
import com.just.goap.effect.EffectContainer;

import java.util.HashMap;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public class MutableWorldState extends WorldState {

    public MutableWorldState() {
        this(new HashMap<>());
    }

    public MutableWorldState(WorldState worldState) {
        this(new HashMap<>(worldState.stateMap));
    }

    public MutableWorldState(HashMap<TypedIdentifier<?>, Object> stateMap) {
        super(stateMap);
    }

    public <T> void set(TypedIdentifier<T> key, T value) {
        stateMap.put(key, value);
    }

    public <T> void set(TypedIdentifier<T> key, UnaryOperator<T> unaryOperator, Supplier<T> supplyIfAbsent) {
        var previousValue = getOrNull(key);
        var mutatedValue = unaryOperator.apply(previousValue == null ? supplyIfAbsent.get() : previousValue);

        set(key, mutatedValue);
    }

    public void apply(EffectContainer effectContainer) {
        effectContainer.getEffects().forEach(effect -> effect.apply(this));
    }
}
