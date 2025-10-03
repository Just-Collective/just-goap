package com.just.goap;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public sealed abstract class GOAPKey<T> {

    public static <T> Derived<T> derived(String id) {
        return new Derived<>(id);
    }

    public static <T> Sensed<T> sensed(String id) {
        return new Sensed<>(id);
    }

    protected final String id;

    protected GOAPKey(String id) {
        this.id = id;
    }

    public abstract Derived<T> asDerived();

    public abstract Sensed<T> asSensed();

    public String id() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof GOAPKey<?> key)) {
            return false;
        }

        return Objects.equals(id, key.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    public static final class Derived<T> extends GOAPKey<T> {

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

    public static final class Sensed<T> extends GOAPKey<T> {

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
