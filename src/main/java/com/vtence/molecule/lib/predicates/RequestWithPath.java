package com.vtence.molecule.lib.predicates;

import com.vtence.molecule.Request;

import java.util.function.Predicate;

public class RequestWithPath implements Predicate<Request> {

    private Predicate<? super String> path;

    public RequestWithPath(Predicate<? super String> path) {
        this.path = path;
    }

    public boolean test(Request actual) {
        return path.test(actual.path());
    }

    public static RequestWithPath withPath(Predicate<? super String> path) {
        return new RequestWithPath(path);
    }
}
