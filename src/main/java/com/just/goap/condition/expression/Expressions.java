package com.just.goap.condition.expression;

import java.util.Objects;
import java.util.function.Predicate;

import com.just.core.functional.range.Range;
import com.just.core.functional.range.RangeSet;

public class Expressions {

    public static final class Boolean {

        private static final Expression<java.lang.Boolean> TRUE = new Expression<>(
            "is true",
            java.lang.Boolean::booleanValue
        );

        private static final Expression<java.lang.Boolean> FALSE = TRUE.negate("is false");

        public static Expression<java.lang.Boolean> isFalse() {
            return FALSE;
        }

        public static Expression<java.lang.Boolean> isTrue() {
            return TRUE;
        }
    }

    public static final class Compare {

        public static <T> Expression<T> doesNotEqual(T expected) {
            return equalTo(expected).negate("does not equal " + expected);
        }

        public static <T> Expression<T> equalTo(T expected) {
            return new Expression<>("equals " + expected, actual -> Objects.equals(expected, actual));
        }

        public static <T extends Comparable<T>> Expression<? super T> lessThan(T threshold) {
            return new Expression<>("less than " + threshold, actual -> actual.compareTo(threshold) < 0);
        }

        public static <T extends Comparable<T>> Expression<? super T> greaterThan(T threshold) {
            return new Expression<>("greater than " + threshold, actual -> actual.compareTo(threshold) > 0);
        }

        public static <T extends Comparable<T>> Expression<? super T> atLeast(T threshold) {
            return greaterThan(threshold).negate("less than or equal to " + threshold);
        }

        public static <T extends Comparable<T>> Expression<? super T> atMost(T threshold) {
            return lessThan(threshold).negate("greater than or equal to " + threshold);
        }

        public static <T extends Comparable<T>> Expression<? super T> inRange(Range<T> range) {
            return new Expression<>(
                "in range " + range,
                range::contains
            );
        }

        public static <T extends Comparable<T>> Expression<? super T> notInRange(Range<T> range) {
            return inRange(range).negate("not in range " + range);
        }

        public static <T extends Comparable<T>> Expression<? super T> inRangeSet(RangeSet<T> set) {
            return new Expression<>(
                set.toString(),
                set::contains
            );
        }

        public static <T extends Comparable<T>> Expression<? super T> notInRangeSet(RangeSet<T> set) {
            return inRangeSet(set).negate("not " + set);
        }
    }

    public static final class Option {

        private static final Expression<com.just.core.functional.option.Option<?>> IS_NONE = new Expression<>(
            "is none",
            com.just.core.functional.option.Option::isNone
        );

        private static final Expression<com.just.core.functional.option.Option<?>> IS_SOME = IS_NONE.negate("is some");

        public static Expression<com.just.core.functional.option.Option<?>> isSome() {
            return IS_SOME;
        }

        public static Expression<com.just.core.functional.option.Option<?>> isNone() {
            return IS_NONE;
        }

        public static <T> Expression<com.just.core.functional.option.Option<T>> value(Expression<? super T> inner) {
            return new Expression<>("option value " + inner, opt -> opt.isSomeAnd(inner::evaluate));
        }
    }

    public static final class Optional {

        private static final Expression<java.util.Optional<?>> IS_EMPTY = new Expression<>(
            "is optional empty",
            java.util.Optional::isEmpty
        );

        private static final Expression<java.util.Optional<?>> IS_PRESENT = IS_EMPTY.negate("is optional present");

        public static Expression<java.util.Optional<?>> isEmpty() {
            return IS_EMPTY;
        }

        public static Expression<java.util.Optional<?>> isPresent() {
            return IS_PRESENT;
        }

        public static <T> Expression<java.util.Optional<T>> value(Expression<? super T> inner) {
            return new Expression<>("value " + inner, opt -> opt.map(inner::evaluate).orElse(false));
        }
    }

    public static final class Collection {

        private static final Expression<java.util.Collection<?>> IS_EMPTY = new Expression<>(
            "is empty",
            java.util.Collection::isEmpty
        );

        private static final Expression<java.util.Collection<?>> IS_NOT_EMPTY = IS_EMPTY.negate("is not empty");

        public static Expression<java.util.Collection<?>> isEmpty() {
            return IS_EMPTY;
        }

        public static Expression<java.util.Collection<?>> isNotEmpty() {
            return IS_NOT_EMPTY;
        }

        public static <T> Expression<java.util.Collection<? extends T>> contains(T expected) {
            return new Expression<>("contains " + expected, collection -> collection.contains(expected));
        }

        public static <T> Expression<java.util.Collection<? extends T>> doesNotContain(T expected) {
            return contains(expected).negate("does not contain " + expected);
        }

        public static <T> Expression<java.util.Collection<? extends T>> allMatch(Expression<? super T> inner) {
            return new Expression<>(
                "all match: (" + inner + ")",
                collection -> collection.stream().allMatch(inner::evaluate)
            );
        }

        public static <T> Expression<java.util.Collection<? extends T>> anyMatch(Expression<? super T> inner) {
            return new Expression<>(
                "any match: (" + inner + ")",
                collection -> collection.stream().anyMatch(inner::evaluate)
            );
        }

        public static <T> Expression<java.util.Collection<? extends T>> noneMatch(Expression<? super T> inner) {
            return new Expression<>(
                "none match: (" + inner + ")",
                collection -> collection.stream().noneMatch(inner::evaluate)
            );
        }
    }

    public static final class Map {

        private static final Expression<java.util.Map<?, ?>> IS_EMPTY = new Expression<>(
            "is empty",
            java.util.Map::isEmpty
        );

        private static final Expression<java.util.Map<?, ?>> IS_NOT_EMPTY = IS_EMPTY.negate("is not empty");

        public static Expression<java.util.Map<?, ?>> isEmpty() {
            return IS_EMPTY;
        }

        public static Expression<java.util.Map<?, ?>> isNotEmpty() {
            return IS_NOT_EMPTY;
        }

        public static <K> Expression<java.util.Map<? extends K, ?>> containsKey(K key) {
            return new Expression<>("contains key: " + key, map -> map.containsKey(key));
        }

        public static <V> Expression<java.util.Map<?, ? extends V>> containsValue(V value) {
            return new Expression<>("contains value: " + value, map -> map.containsValue(value));
        }

        public static <K> Expression<java.util.Map<? extends K, ?>> allKeysMatch(Expression<? super K> inner) {
            return new Expression<>(
                "all keys match: (" + inner + ")",
                map -> map.keySet().stream().allMatch(inner::evaluate)
            );
        }

        public static <K> Expression<java.util.Map<? extends K, ?>> anyKeyMatches(Expression<? super K> inner) {
            return new Expression<>(
                "any key matches: (" + inner + ")",
                map -> map.keySet().stream().anyMatch(inner::evaluate)
            );
        }

        public static <K> Expression<java.util.Map<? extends K, ?>> noneKeyMatches(Expression<? super K> inner) {
            return new Expression<>(
                "no key matches: (" + inner + ")",
                map -> map.keySet().stream().noneMatch(inner::evaluate)
            );
        }

        public static <K, V> Expression<java.util.Map<K, V>> keyValueMatches(K key, Expression<? super V> inner) {
            return new Expression<>(
                "key " + key + " maps to value matching (" + inner + ")",
                map -> map.containsKey(key) && inner.evaluate(map.get(key))
            );
        }

        public static <K, V> Expression<java.util.Map<K, V>> allValuesMatch(Expression<? super V> inner) {
            return new Expression<>(
                "all values match: (" + inner + ")",
                map -> map.values().stream().allMatch(inner::evaluate)
            );
        }

        public static <K, V> Expression<java.util.Map<K, V>> anyValueMatches(Expression<? super V> inner) {
            return new Expression<>(
                "any value matches: (" + inner + ")",
                map -> map.values().stream().anyMatch(inner::evaluate)
            );
        }

        public static <K, V> Expression<java.util.Map<K, V>> noneValueMatches(Expression<? super V> inner) {
            return new Expression<>(
                "no values match: (" + inner + ")",
                map -> map.values().stream().noneMatch(inner::evaluate)
            );
        }
    }

    public static <T> Expression<T> of(String description, Predicate<? super T> predicate) {
        return new Expression<>(description, predicate);
    }

    private Expressions() {
        throw new UnsupportedOperationException();
    }
}
