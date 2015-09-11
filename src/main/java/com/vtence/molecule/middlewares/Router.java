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

public class Router extends AbstractMiddleware implements RouteSet {

    public static Router draw(RouteBuilder routeBuilder) {
        Router router = new Router();
        routeBuilder.build(router);
        return router;
    }

    private final List<Route> routingTable = new ArrayList<>();

    public Router() {
        this(new NotFound());
    }

    public Router(final Application fallback) {
        connectTo(fallback);
    }

    public Router defaultsTo(Application app) {
        connectTo(app);
        return this;
    }

    public void add(Route route) {
        routingTable.add(route);
    }

    private Optional<Route> routeFor(Request request) {
        return routingTable.stream().filter(route -> route.matches(request)).findFirst();
    }

    public void handle(Request request, Response response) throws Exception {
        Optional<Route> route = routeFor(request);
        if (route.isPresent())
            route.get().handle(request, response);
        else
            forward(request, response);
    }
}