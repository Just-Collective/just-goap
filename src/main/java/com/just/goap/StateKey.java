package com.just.goap;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public sealed abstract class StateKey<T> {

    private static final int UNSET = Integer.MIN_VALUE;

    public static <T> Derived<T> derived(String id) {
        return new Derived<>(id);
    }

    public static <T> Sensed<T> sensed(String id) {
        return new Sensed<>(id);
    }

    protected final String id;

    private transient int cachedHashCode = UNSET;

    protected StateKey(String id) {
        this.id = id;
    }

    public abstract Derived<T> asDerived();

    public abstract Sensed<T> asSensed();

    public String id() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof StateKey<?> key)) {
            return false;
        }

        return Objects.equals(id, key.id);
    }

    @Override
    public int hashCode() {
        var hashCode = cachedHashCode;

        if (hashCode == UNSET) {
            hashCode = id.hashCode();
            cachedHashCode = hashCode;
        }

        return hashCode;
    }

    public static final class Derived<T> extends StateKey<T> {

        private Derived(String id) {
            super(id);
        }

        @Override
        public Derived<T> asDerived() {
            return this;
        }

        @Override
        public Sensed<T> asSensed() {
            return new Sensed<>(id);
        }

        @Override
        public @NotNull String toString() {
            return "derived:" + id;
        }
    }

    public static final class Sensed<T> extends StateKey<T> {

        private Sensed(String id) {
            super(id);
        }

        @Override
        public Derived<T> asDerived() {
            return new Derived<>(id);
        }

        @Override
        public Sensed<T> asSensed() {
            return this;
        }

        @Override
        public @NotNull String toString() {
            return "sensed:" + id;
        }
    }
}
