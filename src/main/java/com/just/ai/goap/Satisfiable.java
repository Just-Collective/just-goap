package com.just.ai.goap;

import com.just.ai.goap.effect.EffectContainer;
import com.just.ai.goap.state.ReadableWorldState;

public interface Satisfiable {

    boolean satisfiedBy(ReadableWorldState worldState);

    default boolean satisfiedBy(EffectContainer effectContainer) {
        return satisfiedBy(effectContainer.toWorldState());
    }
}
