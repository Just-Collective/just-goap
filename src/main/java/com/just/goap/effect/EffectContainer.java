package com.just.goap.effect;

import com.just.goap.state.MutableWorldState;
import com.just.goap.state.WorldState;

import java.util.Arrays;
import java.util.List;

public class EffectContainer {

    public static EffectContainer of(Effect<?>... effects) {
        return new EffectContainer(Arrays.stream(effects).toList());
    }

    protected final List<Effect<?>> effects;

    protected EffectContainer(List<Effect<?>> effects) {
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
