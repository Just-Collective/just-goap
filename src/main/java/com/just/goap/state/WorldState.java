package com.just.goap.state;

import com.just.goap.TypedIdentifier;
import com.just.goap.condition.ConditionContainer;
import com.just.goap.effect.EffectContainer;

import java.util.Map;

public class WorldState extends StateCache {

    public WorldState(Map<TypedIdentifier<?>, Object> stateMap) {
        super(stateMap);
    }

    public boolean satisfiedBy(WorldState worldState) {
        for (var entry : stateMap.entrySet()) {
            var otherValue = worldState.getOrNull(entry.getKey());

            if (!entry.getValue().equals(otherValue)) {
                return false;
            }
        }

        return true;
    }

    public boolean satisfies(EffectContainer effectContainer) {
        return effectContainer.toWorldState()
            .satisfiedBy(this);
    }

    public boolean satisfies(ConditionContainer conditionContainer) {
        return conditionContainer.satisfiedBy(this);
    }
}
