package com.just.goap;

import com.just.goap.effect.EffectContainer;
import com.just.goap.state.ReadableWorldState;

public interface Satisfiable {

    boolean satisfiedBy(ReadableWorldState worldState);

    default boolean satisfiedBy(EffectContainer effectContainer) {
        return satisfiedBy(effectContainer.toWorldState());
    }
}
