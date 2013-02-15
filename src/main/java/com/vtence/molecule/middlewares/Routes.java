package com.vtence.molecule.middlewares;

import com.vtence.molecule.Application;
import com.vtence.molecule.Request;
import com.vtence.molecule.Response;
import com.vtence.molecule.routing.Route;
import com.vtence.molecule.routing.RouteBuilder;
import com.vtence.molecule.routing.RouteSet;

import java.util.ArrayList;
import java.util.List;

public class Routes extends AbstractMiddleware implements RouteSet {

    public static Routes draw(RouteBuilder routeBuilder) {
        Routes routes = new Routes();
        routeBuilder.build(routes);
        return routes;
    }

    private final List<Route> routingTable = new ArrayList<Route>();

    public Routes() {
        this(new NotFound());
    }

    public Routes(final Application fallback) {
        connectTo(fallback);
    }

    public Routes defaultsTo(Application app) {
        connectTo(app);
        return this;
    }

    public void add(Route route) {
        routingTable.add(route);
    }

    private Route routeFor(Request request) {
        for (Route route : routingTable) {
            if (route.matches(request)) return route;
        }
        return null;
    }

    public void handle(Request request, Response response) throws Exception {
        Route route = routeFor(request);
        if (route != null)
            route.handle(request, response);
        else
            forward(request, response);
    }
}