package com.vtence.molecule.routing;

import com.vtence.molecule.Application;
import com.vtence.molecule.http.HttpMethod;
import com.vtence.molecule.lib.predicates.Predicates;

import java.util.function.Predicate;
import java.util.stream.Stream;

import static com.vtence.molecule.lib.predicates.Predicates.none;

public class RouteDefinition implements ViaClause {

    private final Predicate<? super String> path;

    private Predicate<? super HttpMethod> method = Predicates.all();
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
        this.method = method;
        return this;
    }

    public RouteDefinition to(Application application) {
        this.app = application;
        return this;
    }

    public Route toRoute() {
        return new DynamicRoute(path, method, app);
    }

    private Predicate<? super HttpMethod> oneOf(HttpMethod... methods) {
        return Stream.of(methods)
                     .map(Predicate::isEqual)
                     .reduce(none(), Predicate::or);
    }
}
