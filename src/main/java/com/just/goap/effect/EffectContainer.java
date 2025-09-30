package com.just.goap.effect;

import com.just.core.functional.function.Lazy;
import com.just.goap.state.ReadableWorldState;
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

    private final Lazy<WorldState> worldStateLazy;

    private EffectContainer(List<Effect<?>> effects) {
        this.effects = effects;
        this.worldStateLazy = Lazy.of(() -> {
            var worldState = WorldState.create();
            worldState.apply(this);
            return worldState;
        });
    }

    public List<Effect<?>> getEffects() {
        return effects;
    }

    public ReadableWorldState toWorldState() {
        return worldStateLazy.get();
    }
}
