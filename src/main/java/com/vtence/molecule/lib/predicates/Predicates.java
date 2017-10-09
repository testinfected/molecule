package com.vtence.molecule.lib.predicates;

import com.vtence.molecule.Request;
import com.vtence.molecule.http.HttpMethod;

import java.util.function.Function;
import java.util.function.Predicate;

import static com.vtence.molecule.http.HeaderNames.ACCEPT;
import static com.vtence.molecule.http.HttpMethod.valueOf;
import static com.vtence.molecule.http.MimeTypes.isSpecializationOf;
import static com.vtence.molecule.lib.predicates.RequestWith.requestWith;
import static java.util.function.Predicate.isEqual;

public final class Predicates {

    public static Predicate<Request> withPath(String path) {
        return withPath(isEqual(path));
    }

    public static Predicate<Request> withPathPrefix(String path) {
        return withPath(test(s -> s.startsWith(path)));
    }

    public static Predicate<Request> withPath(Predicate<? super String> path) {
        return requestWith(Request::path, path);
    }

    public static Predicate<Request> withMethod(String name) {
        return withMethod(valueOf(name.toUpperCase()));
    }

    public static Predicate<Request> withMethod(HttpMethod method) {
        return withMethod(isEqual(method));
    }

    public static Predicate<Request> withMethod(Predicate<? super HttpMethod> method) {
        return requestWith(Request::method, method);
    }

    public static Predicate<Request> accepting(String mimeType) {
        return accepting(isSpecializationOf(mimeType));
    }

    public static Predicate<Request> accepting(Predicate<? super String> mimeType) {
        return requestWith(r -> r.header(ACCEPT), mimeType);
    }

    public static Predicate<Request> GET(String path) {
        return GET(isEqual(path));
    }

    public static Predicate<Request> GET(Predicate<? super String> path) {
        return withMethod(HttpMethod.GET).and(withPath(path));
    }

    public static Predicate<Request> POST(String path) {
        return POST(isEqual(path));
    }

    public static Predicate<Request> POST(Predicate<? super String> path) {
        return withMethod(HttpMethod.POST).and(withPath(path));
    }

    public static Predicate<Request> PUT(String path) {
        return PUT(isEqual(path));
    }

    public static Predicate<Request> PUT(Predicate<? super String> path) {
        return withMethod(HttpMethod.PUT).and(withPath(path));
    }

    public static Predicate<Request> PATCH(String path) {
        return PATCH(isEqual(path));
    }

    public static Predicate<Request> PATCH(Predicate<? super String> path) {
        return withMethod(HttpMethod.PATCH).and(withPath(path));
    }

    public static Predicate<Request> DELETE(String path) {
        return DELETE(isEqual(path));
    }

    public static Predicate<Request> DELETE(Predicate<? super String> path) {
        return withMethod(HttpMethod.DELETE).and(withPath(path));
    }

    public static Predicate<Request> HEAD(String path) {
        return HEAD(isEqual(path));
    }

    public static Predicate<Request> HEAD(Predicate<? super String> path) {
        return withMethod(HttpMethod.HEAD).and(withPath(path));
    }

    public static Predicate<Request> OPTIONS(String path) {
        return OPTIONS(isEqual(path));
    }

    public static Predicate<Request> OPTIONS(Predicate<? super String> path) {
        return withMethod(HttpMethod.OPTIONS).and(withPath(path));
    }

    public static <T> Predicate<T> anything() {
        return t -> true;
    }

    public static <T> Predicate<T> nothing() {
        return t -> false;
    }

    public static <T> Predicate<T> test(Function<T, Boolean> condition) {
        return condition::apply;
    }

    Predicates() {}
}
