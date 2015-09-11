package com.vtence.molecule.lib.matchers;

public class Combination<T> implements Matcher<T> {

    private final Matcher<? super T> matcher;

    public Combination(Matcher<? super T> matcher) {
        this.matcher = matcher;
    }

    public boolean matches(T actual) {
        return matcher.matches(actual);
    }

    public static <LHS> Combination<LHS> both(Matcher<? super LHS> matcher) {
        return new Combination<>(matcher);
    }

    @SuppressWarnings("unchecked")
    public Combination<T> and(Matcher<? super T> other) {
        return new Combination<T>(Matchers.allOf(matcher, other));
    }
}
