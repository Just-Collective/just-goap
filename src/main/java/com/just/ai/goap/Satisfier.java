package com.just.ai.goap;

import com.just.ai.goap.condition.ConditionContainer;
import com.just.ai.goap.effect.EffectContainer;

public interface Satisfier {

    boolean satisfies(EffectContainer effectContainer);

    boolean satisfies(ConditionContainer conditionContainer);
}
