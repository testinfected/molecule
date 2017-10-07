package com.vtence.molecule.lib.predicates;

import com.vtence.molecule.Request;
import com.vtence.molecule.http.HttpMethod;

import java.util.function.Predicate;

public final class Predicates {

    public static Predicate<Request> withPath(Predicate<? super String> path) {
        return RequestWithPath.withPath(path);
    }

    public static Predicate<String> startingWith(String prefix) {
        return StartingWith.startingWith(prefix);
    }

    public static Predicate<Request> withMethod(String name) {
        return RequestWithMethod.withMethod(name);
    }

    public static Predicate<Request> withMethod(HttpMethod method) {
        return RequestWithMethod.withMethod(method);
    }

    public static Predicate<Request> withMethod(Predicate<? super HttpMethod> method) {
        return RequestWithMethod.withMethod(method);
    }

    public static <T> Predicate<T> all() {
        return T -> true;
    }

    public static <T> Predicate<T> none() {
        return T -> false;
    }

    Predicates() {}
}
