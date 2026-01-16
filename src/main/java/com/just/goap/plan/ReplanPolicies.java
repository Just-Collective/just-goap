package com.just.goap.plan;

import java.util.function.Predicate;

public class ReplanPolicies {

    public static <T> ReplanPolicy<T> always() {
        return context -> true;
    }

    public static <T> ReplanPolicy<T> never() {
        return context -> false;
    }

    public static <T> ReplanPolicy<T> ifNoActivePlans() {
        return context -> !context.agent().getPlanExecutor().hasActivePlans();
    }

    public static <T> ReplanPolicy<T> custom(Predicate<ReplanPolicy.Context<T>> predicate) {
        return predicate::test;
    }

    @SafeVarargs
    public static <T> ReplanPolicy<T> allOf(
        ReplanPolicy<T>... replanPolicies
    ) {
        return context -> {
            for (var policy : replanPolicies) {
                if (!policy.shouldReplan(context)) {
                    return false;
                }
            }

            return true;
        };
    }

    @SafeVarargs
    public static <T> ReplanPolicy<T> anyOf(
        ReplanPolicy<T>... replanPolicies
    ) {
        return context -> {
            for (var policy : replanPolicies) {
                if (policy.shouldReplan(context)) {
                    return true;
                }
            }

            return false;
        };
    }

    private ReplanPolicies() {
        throw new UnsupportedOperationException();
    }
}
