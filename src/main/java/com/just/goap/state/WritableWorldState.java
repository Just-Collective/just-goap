package com.just.goap.state;

import com.just.goap.TypedIdentifier;
import com.just.goap.effect.EffectContainer;

public interface WritableWorldState {

    <T> void set(TypedIdentifier<T> key, T value);

    void apply(EffectContainer effectContainer);

    WritableWorldState copy();
}
