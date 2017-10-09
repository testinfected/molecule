package com.vtence.molecule.routing;

import com.vtence.molecule.Application;
import com.vtence.molecule.Request;
import com.vtence.molecule.http.HttpMethod;
import com.vtence.molecule.lib.predicates.Predicates;

import java.util.function.Predicate;
import java.util.stream.Stream;

import static com.vtence.molecule.http.MimeTypes.isSpecializationOf;
import static com.vtence.molecule.lib.predicates.Predicates.accepting;
import static com.vtence.molecule.lib.predicates.Predicates.nothing;
import static com.vtence.molecule.lib.predicates.Predicates.withMethod;

public class RouteDefinition implements ViaClause {

    private final Predicate<? super String> path;

    private Predicate<Request> conditions = Predicates.anything();
    private Application app;

    private RouteDefinition(Predicate<? super String> path) {
        this.path = path;
    }

    public static RouteDefinition map(Predicate<? super String> path) {
        return new RouteDefinition(path);
    }

    public RouteDefinition via(HttpMethod... methods) {
        return via(oneOf(methods));
    }

    public RouteDefinition via(Predicate<? super HttpMethod> method) {
        return and(withMethod(method));
    }

    public RouteDefinition accept(String mimeType) {
        return accept(isSpecializationOf(mimeType));
    }

    public RouteDefinition accept(Predicate<? super String> mimeType) {
        return and(accepting(mimeType));
    }

    public RouteDefinition to(Application application) {
        this.app = application;
        return this;
    }

    public Route toRoute() {
        return new DynamicRoute(path, conditions, app);
    }

    private RouteDefinition and(Predicate<Request> accepting) {
        conditions = conditions.and(accepting);
        return this;
    }

    private Predicate<? super HttpMethod> oneOf(HttpMethod... methods) {
        return Stream.of(methods)
                     .map(Predicate::isEqual)
                     .reduce(nothing(), Predicate::or);
    }
}
