package com.vtence.molecule.middlewares;

import com.vtence.molecule.Application;
import com.vtence.molecule.Request;
import com.vtence.molecule.Response;
import com.vtence.molecule.routing.Route;
import com.vtence.molecule.routing.RouteBuilder;
import com.vtence.molecule.routing.RouteSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class Router implements Application, RouteSet {

    public static Router draw(RouteBuilder routeBuilder) {
        Router router = new Router();
        routeBuilder.build(router);
        return router;
    }

    private final List<Route> routingTable = new ArrayList<>();
    private final Application fallback;

    public Router() {
        this(new NotFound());
    }

    public Router(final Application fallback) {
        this.fallback = fallback;
    }

    public Router route(Predicate<Request> condition, Application app) {
        return route(new StaticRoute(condition, app));
    }

    public Router route(Route route) {
        routingTable.add(route);
        return this;
    }

    public Response handle(Request request) throws Exception {
        return routeFor(request).handle(request);
    }

    private Application routeFor(Request request) {
        return routingTable.stream()
                           .map(route -> route.route(request))
                           .filter(Optional::isPresent)
                           .map(Optional::get)
                           .findFirst()
                           .orElse(fallback);
    }
}