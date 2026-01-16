package com.just.goap.plan;

public class ReplanPolicies {

    public static <T> ReplanPolicy<T> always() {
        return context -> true;
    }

    public static <T> ReplanPolicy<T> never() {
        return context -> false;
    }

    public static <T> ReplanPolicy<T> ifNoActivePlans() {
        return context -> !context.hasActivePlans();
    }

    private ReplanPolicies() {
        throw new UnsupportedOperationException();
    }
}
