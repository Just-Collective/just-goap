package com.just.goap.effect;

import java.util.ArrayList;

public class MutableEffectContainer extends EffectContainer {

    public MutableEffectContainer() {
        super(new ArrayList<>());
    }

    public void addEffect(Effect<?> effect) {
        effects.add(effect);
    }
}
