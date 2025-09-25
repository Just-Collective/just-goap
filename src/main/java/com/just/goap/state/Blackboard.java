package com.just.goap.state;

import com.just.goap.TypedIdentifier;

import java.util.HashMap;

public class Blackboard extends StateCache {

    public Blackboard() {
        super(new HashMap<>());
    }

    public <T> void set(TypedIdentifier<T> key, T value) {
        stateMap.put(key, value);
    }
}
