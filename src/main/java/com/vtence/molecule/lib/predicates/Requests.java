package com.vtence.molecule.lib.predicates;

import com.vtence.molecule.Request;
import com.vtence.molecule.http.HttpMethod;
import com.vtence.molecule.routing.DynamicPath;
import com.vtence.molecule.routing.WithBoundParameters;

import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.vtence.molecule.http.HeaderNames.ACCEPT;
import static com.vtence.molecule.http.HttpMethod.valueOf;
import static com.vtence.molecule.http.MimeTypes.isSpecializationOf;
import static java.util.function.Predicate.isEqual;

public final class Requests {

    public static Predicate<Request> withPath(String path) {
        return withPath(isEqual(path));
    }

    public static Predicate<Request> withDynamicPath(String pattern) {
        return withDynamicPath(DynamicPath.equalTo(pattern));
    }

    public static Predicate<Request> withDynamicPath(DynamicPath path) {
        return new WithDynamicPath(path);
    }

    public static Predicate<Request> withPathPrefix(String path) {
        return withPath(test(it -> it.startsWith(path)));
    }

    public static Predicate<Request> withPath(Predicate<? super String> path) {
        return new RequestWith<>(Request::path, path);
    }

    public static Predicate<Request> withMethod(String name) {
        return withMethod(valueOf(name.toUpperCase()));
    }

    public static Predicate<Request> withMethod(HttpMethod method) {
        return withMethod(isEqual(method));
    }

    public static Predicate<Request> withMethod(Predicate<? super HttpMethod> method) {
        return new RequestWith<>(Request::method, method);
    }

    public static Predicate<Request> accepting(String mimeType) {
        return accepting(isSpecializationOf(mimeType));
    }

    public static Predicate<Request> accepting(Predicate<? super String> mimeType) {
        return new RequestWith<>(it -> it.header(ACCEPT), mimeType);
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

    private static class WithDynamicPath implements Predicate<Request>, WithBoundParameters {

        private final DynamicPath path;

        WithDynamicPath(DynamicPath path) {
            this.path = path;
        }

        @Override
        public Map<String, String> parametersBoundTo(String path) {
            return this.path.parametersBoundTo(path);
        }

        @Override
        public boolean test(Request request) {
            return Requests.<String>test(Objects::nonNull)
                           .and(path)
                           .test(request.path());
        }
    }

    private static class RequestWith<T> implements Predicate<Request> {

        private final Function<Request, T> feature;
        private final Predicate<? super T> condition;

        public RequestWith(Function<Request, T> feature, Predicate<? super T> condition) {
            this.feature = feature;
            this.condition = condition;
        }

        public boolean test(Request request) {
            return Requests.<T>test(Objects::nonNull)
                           .and(condition)
                           .test(feature.apply(request));
        }
    }

    Requests() {}
}
