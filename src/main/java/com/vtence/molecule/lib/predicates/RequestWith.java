package com.vtence.molecule.lib.predicates;

import com.vtence.molecule.Request;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

public class RequestWith<T> implements Predicate<Request> {

    private final Function<Request, T> feature;
    private final Predicate<? super T> condition;

    public RequestWith(Function<Request, T> feature, Predicate<? super T> condition) {
        this.feature = feature;
        this.condition = condition;
    }

    public boolean test(Request request) {
        return Predicates.<T>test(Objects::nonNull).and(condition)
                                                   .test(feature.apply(request));
    }

    public static <T> RequestWith<T> requestWith(Function<Request, T> feature, Predicate<? super T> matching) {
        return new RequestWith<>(feature, matching);
    }
}
