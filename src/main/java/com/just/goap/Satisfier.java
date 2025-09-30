package com.just.goap;

import com.just.goap.condition.ConditionContainer;
import com.just.goap.effect.EffectContainer;

public interface Satisfier {

    boolean satisfies(EffectContainer effectContainer);

    boolean satisfies(ConditionContainer conditionContainer);
}
