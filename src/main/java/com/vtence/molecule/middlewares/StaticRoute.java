package com.vtence.molecule.middlewares;

import com.vtence.molecule.Application;
import com.vtence.molecule.Request;
import com.vtence.molecule.routing.Route;

import java.util.Optional;
import java.util.function.Predicate;

public class StaticRoute implements Route {

    private final Predicate<Request> condition;
    private final Application app;

    public StaticRoute(Predicate<Request> condition, Application app) {
        this.condition = condition;
        this.app = app;
    }

    public Optional<Application> route(Request request) {
        return condition.test(request) ? Optional.of(app): Optional.empty();
    }
}
