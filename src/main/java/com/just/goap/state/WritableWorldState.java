package com.just.goap.state;

import com.just.goap.StateKey;
import com.just.goap.effect.EffectContainer;

public interface WritableWorldState {

    <T> void set(StateKey<T> key, T value);

    void apply(EffectContainer effectContainer);

    WritableWorldState copy();
}
