package com.just.goap;

import com.just.goap.state.GOAPMutableWorldState;

@FunctionalInterface
public interface GOAPSensor<T> {

    void sense(T context, GOAPMutableWorldState worldState);
}
