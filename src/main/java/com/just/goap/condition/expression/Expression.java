package com.just.goap.condition.expression;

import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

public final class Expression<T> {

    private final String description;

    private final Predicate<? super T> predicate;

    Expression(String description, Predicate<? super T> predicate) {
        this.description = description;
        this.predicate = predicate;
    }

    public boolean evaluate(@NotNull T actual) {
        return predicate.test(actual);
    }

    @Override
    public @NotNull String toString() {
        return description;
    }

    public <U extends T> Expression<U> and(Expression<? super U> other) {
        return new Expression<>(
            description + " && " + other.description,
            value -> evaluate(value) && other.evaluate(value)
        );
    }

    public <U extends T> Expression<U> or(Expression<? super U> other) {
        return new Expression<>(
            description + " || " + other.description,
            value -> evaluate(value) || other.evaluate(value)
        );
    }

    public Expression<T> negate(String newDescription) {
        return new Expression<>(newDescription, Predicate.not(this::evaluate));
    }
}
