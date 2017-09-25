package com.vtence.molecule.middlewares;

import com.vtence.molecule.Application;
import com.vtence.molecule.Request;
import com.vtence.molecule.Response;
import com.vtence.molecule.routing.Route;
import com.vtence.molecule.routing.RouteBuilder;
import com.vtence.molecule.routing.RouteSet;

import java.util.ArrayList;
import java.util.List;

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

    public void add(Route route) {
        routingTable.add(route);
    }

    public Response handle(Request request) throws Exception {
        return routeFor(request).handle(request);
    }

    public void handle(Request request, Response response) throws Exception {
        routeFor(request).handle(request, response);
    }

    private Route routeFor(Request request) {
        return routingTable.stream().filter(route -> route.matches(request))
                           .findFirst()
                           .orElse(new FallbackRoute(fallback));
    }

    private class FallbackRoute implements Route {
        private final Application fallback;

        public FallbackRoute(Application fallback) {
            this.fallback = fallback;
        }

        public Response handle(Request request) throws Exception {
            return fallback.handle(request);
        }

        public void handle(Request request, Response response) throws Exception {
            fallback.handle(request, response);
        }

        public boolean matches(Request actual) {
            return true;
        }
    }
}