package com.vtence.molecule.lib.predicates;

import com.vtence.molecule.Request;
import com.vtence.molecule.http.HttpMethod;

import java.util.function.Predicate;

public class RequestWithMethod implements Predicate<Request> {

    private final Predicate<? super HttpMethod> method;

    public RequestWithMethod(Predicate<? super HttpMethod> method) {
        this.method = method;
    }

    public boolean test(Request actual) {
        return method.test(actual.method());
    }

    public static RequestWithMethod withMethod(String name) {
        return withMethod(HttpMethod.valueOf(name.toUpperCase()));
    }

    public static RequestWithMethod withMethod(HttpMethod method) {
        return withMethod(Predicate.isEqual(method));
    }

    public static RequestWithMethod withMethod(Predicate<? super HttpMethod> method) {
        return new RequestWithMethod(method);
    }
}