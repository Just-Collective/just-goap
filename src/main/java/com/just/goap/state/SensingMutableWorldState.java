package com.just.goap.state;

import com.just.goap.Sensor;
import com.just.goap.TypedIdentifier;
import com.just.goap.effect.EffectContainer;

import java.util.Map;

public final class SensingMutableWorldState<T> implements ReadableWorldState, WritableWorldState {

    private final T context;

    private final Map<TypedIdentifier<?>, Sensor<T, ?>> sensorsByIdentifierMap;

    private final MutableWorldState mutableWorldState;

    public SensingMutableWorldState(T context, Map<TypedIdentifier<?>, Sensor<T, ?>> sensorsByIdentifierMap) {
        this(context, sensorsByIdentifierMap, new MutableWorldState());
    }

    private SensingMutableWorldState(
        T context,
        Map<TypedIdentifier<?>, Sensor<T, ?>> sensorsByIdentifierMap,
        MutableWorldState mutableWorldState
    ) {
        this.context = context;
        this.sensorsByIdentifierMap = sensorsByIdentifierMap;
        this.mutableWorldState = mutableWorldState;
    }

    @Override
    public <U> U getOrNull(TypedIdentifier<U> typedIdentifier) {
        var value = mutableWorldState.getOrNull(typedIdentifier);

        if (value == null) {
            @SuppressWarnings("unchecked")
            var sensor = (Sensor<T, U>) sensorsByIdentifierMap.get(typedIdentifier);

            switch (sensor) {
                case Sensor.Derived<T, U, ?> derived -> {
                    @SuppressWarnings("unchecked")
                    var castedDerived = ((Sensor.Derived<T, U, Object>) derived);
                    var sourceValue = getOrNull(castedDerived.sourceIdentifier());
                    value = castedDerived.extractor().apply(context, sourceValue);
                    set(typedIdentifier, value);
                }
                case Sensor.Direct<T, U> direct -> {
                    value = direct.extractor().apply(context);
                    set(typedIdentifier, value);
                }
            }
        }

        return value;
    }

    @Override
    public Map<TypedIdentifier<?>, ?> getMap() {
        return mutableWorldState.getMap();
    }

    @Override
    public <U> void set(TypedIdentifier<U> key, U value) {
        mutableWorldState.set(key, value);
    }

    @Override
    public void apply(EffectContainer effectContainer) {
        mutableWorldState.apply(effectContainer);
    }

    @Override
    public SensingMutableWorldState<T> copy() {
        return new SensingMutableWorldState<>(context, sensorsByIdentifierMap, mutableWorldState.copy());
    }

    @Override
    public String toString() {
        return "WorldState{" +
            "stateMap=" + getMap() +
            '}';
    }

}
