package com.just.goap.sensor;

import java.util.Set;

import com.just.goap.StateKey;
import com.just.goap.state.ReadableWorldState;

public interface Sensor<T> {

    Set<StateKey.Sensed<?>> outputKeys();

    <V> V apply(StateKey<V> key, T context, ReadableWorldState worldState);

    interface Mono<T, O> extends Sensor<T> {

        StateKey.Sensed<O> key();

        @Override
        default Set<StateKey.Sensed<?>> outputKeys() {
            return Set.of(key());
        }
    }

    interface Multi<T> extends Sensor<T> {}
}
