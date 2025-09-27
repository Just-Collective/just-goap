package com.just.goap.state;

import com.just.goap.GOAPKey;
import com.just.goap.effect.EffectContainer;

public interface WritableWorldState {

    <T> void set(GOAPKey<T> key, T value);

    void apply(EffectContainer effectContainer);

    WritableWorldState copy();
}
