package com.just.goap;

import com.just.goap.state.MutableWorldState;

@FunctionalInterface
public interface Sensor<T> {

    void sense(T context, MutableWorldState worldState);
}
