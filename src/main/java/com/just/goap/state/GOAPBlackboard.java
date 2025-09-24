package com.just.goap.state;

import com.just.goap.TypedIdentifier;

import java.util.HashMap;

public class GOAPBlackboard extends GOAPStateCache {

    public GOAPBlackboard() {
        super(new HashMap<>());
    }

    public <T> void set(TypedIdentifier<T> key, T value) {
        stateMap.put(key, value);
    }
}
