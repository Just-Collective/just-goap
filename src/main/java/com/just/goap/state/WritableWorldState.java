package com.just.goap.state;

import java.util.Map;

import com.just.goap.StateKey;
import com.just.goap.effect.EffectContainer;

public interface WritableWorldState {

    <T> void set(StateKey<T> key, T value);

    void setAll(Map<StateKey<?>, Object> map);

    void apply(EffectContainer effectContainer);

    void clear();
}
