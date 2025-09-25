package com.just.goap.condition.expression;

import com.just.core.functional.option.Option;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Objects;
import java.util.function.Predicate;

public sealed interface Expression<T> {

    static <T> Contains<T> contains(T expected) {
        return new Contains<>(expected);
    }

    static <T> DoesNotEqual<T> doesNotEqual(T expected) {
        return new DoesNotEqual<>(expected);
    }

    static <T> Equals<T> equalTo(T expected) {
        return new Equals<>(expected);
    }

    static Equals<Boolean> isFalse() {
        return equalTo(false);
    }

    static Equals<Boolean> isTrue() {
        return equalTo(true);
    }

    static <T extends Comparable<T>> LessThan<T> lessThan(T expected) {
        return new LessThan<>(expected);
    }

    static <T extends Comparable<T>> GreaterThan<T> greaterThan(T expected) {
        return new GreaterThan<>(expected);
    }

    static <T extends Option<?>> IsSome<T> isSome() {
        return new IsSome<>();
    }

    static <T extends Option<?>> IsNone<T> isNone() {
        return new IsNone<>();
    }

    static <T> Expression<T> where(Predicate<? super T> predicate, String description) {
        return new Where<>(predicate, description);
    }

    boolean evaluate(@NotNull T actual);

    final class Contains<T> implements Expression<Collection<T>> {

        private final T expected;

        private Contains(T expected) {
            this.expected = expected;
        }

        @Override
        public boolean evaluate(@NotNull Collection<T> actual) {
            return actual.contains(expected);
        }

        @Override
        public String toString() {
            return "contains " + expected;
        }
    }

    final class DoesNotEqual<T> implements Expression<T> {

        private final T expected;

        private DoesNotEqual(T expected) {
            this.expected = expected;
        }

        @Override
        public boolean evaluate(@NotNull T actual) {
            return !Objects.equals(expected, actual);
        }

        @Override
        public String toString() {
            return "does not equal " + expected;
        }
    }

    final class Equals<T> implements Expression<T> {

        private final T expected;

        private Equals(T expected) {
            this.expected = expected;
        }

        @Override
        public boolean evaluate(@NotNull T actual) {
            return Objects.equals(expected, actual);
        }

        @Override
        public String toString() {
            return "equals " + expected;
        }
    }

    final class GreaterThan<T extends Comparable<T>> implements Expression<T> {

        private final T threshold;

        private GreaterThan(T threshold) {
            this.threshold = threshold;
        }

        @Override
        public boolean evaluate(@NotNull T actual) {
            return actual.compareTo(threshold) > 0;
        }

        @Override
        public String toString() {
            return "greater than " + threshold;
        }
    }

    final class IsSome<T extends Option<?>> implements Expression<T> {

        private IsSome() {}

        @Override
        public boolean evaluate(@NotNull T actual) {
            return actual.isSome();
        }

        @Override
        public String toString() {
            return "is some";
        }
    }

    final class IsNone<T extends Option<?>> implements Expression<T> {

        private IsNone() {}

        @Override
        public boolean evaluate(@NotNull T actual) {
            return actual.isNone();
        }

        @Override
        public String toString() {
            return "is none";
        }
    }

    final class LessThan<T extends Comparable<T>> implements Expression<T> {

        private final T threshold;

        private LessThan(T threshold) {
            this.threshold = threshold;
        }

        @Override
        public boolean evaluate(@NotNull T actual) {
            return actual.compareTo(threshold) < 0;
        }

        @Override
        public String toString() {
            return "less than " + threshold;
        }
    }

    final class Where<T> implements Expression<T> {

        private final Predicate<? super T> predicate;

        private final String description;

        private Where(Predicate<? super T> predicate, String description) {
            this.predicate = predicate;
            this.description = description;
        }

        @Override
        public boolean evaluate(@NotNull T actual) {
            return predicate.test(actual);
        }

        @Override
        public String toString() {
            return "where " + description;
        }
    }
}
