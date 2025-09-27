package com.just.goap.effect;

import com.just.goap.state.MutableWorldState;
import com.just.goap.state.WorldState;

import java.util.List;

public final class EffectContainer {

    public static EffectContainer of(Effect<?>... effects) {
        return of(List.of(effects));
    }

    public static EffectContainer of(List<Effect<?>> effects) {
        return new EffectContainer(effects);
    }

    private final List<Effect<?>> effects;

    private EffectContainer(List<Effect<?>> effects) {
        this.effects = effects;
    }

    public List<Effect<?>> getEffects() {
        return effects;
    }

    public WorldState toWorldState() {
        var worldState = new MutableWorldState();
        worldState.apply(this);
        return worldState;
    }
}
