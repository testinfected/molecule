package com.vtence.molecule.routing;

import com.vtence.molecule.Application;
import com.vtence.molecule.http.HttpMethod;
import com.vtence.molecule.lib.matchers.Matcher;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.vtence.molecule.http.HttpMethod.*;
import static com.vtence.molecule.lib.matchers.Matchers.anyOf;
import static com.vtence.molecule.lib.matchers.Matchers.anything;
import static com.vtence.molecule.lib.matchers.Matchers.equalTo;

public class DynamicRoutes implements RouteBuilder {

    private final Collection<Definition> routes = new ArrayList<Definition>();

    public void build(RouteSet routeSet) {
        for (Definition definition : this.routes) {
            routeSet.add(definition.toRoute());
        }
    }

    public ViaClause map(String path) {
        return openRoute(new DynamicPath(path));
    }

    public ViaClause map(Matcher<? super String> path) {
        return openRoute(path);
    }

    public ToClause get(String path) {
        return get(new DynamicPath(path));
    }

    public ToClause get(Matcher<? super String> path) {
        return map(path).via(GET);
    }

    public ToClause post(String path) {
        return post(new DynamicPath(path));
    }

    public ToClause post(Matcher<? super String> path) {
        return map(path).via(POST);
    }

    public ToClause put(String path) {
        return put(new DynamicPath(path));
    }

    public ToClause put(Matcher<? super String> path) {
        return map(path).via(PUT);
    }

    public ToClause delete(String path) {
        return delete(new DynamicPath(path));
    }

    public ToClause delete(Matcher<? super String> path) {
        return map(path).via(DELETE);
    }

    public ToClause patch(String path) {
        return patch(new DynamicPath(path));
    }

    public ToClause patch(Matcher<? super String> path) {
        return map(path).via(PATCH);
    }

    public ToClause head(String path) {
        return head(new DynamicPath(path));
    }

    public ToClause head(Matcher<? super String> path) {
        return map(path).via(HEAD);
    }

    public ToClause options(String path) {
        return options(new DynamicPath(path));
    }

    public ToClause options(Matcher<? super String> path) {
        return map(path).via(OPTIONS);
    }

    private Definition openRoute(Matcher<? super String> path) {
        Definition definition = new Definition(path);
        routes.add(definition);
        return definition;
    }

    private static class Definition implements ViaClause {

        private final Matcher<? super String> path;

        private Matcher<? super HttpMethod> method = anything();
        private Application app;

        public Definition(Matcher<? super String> path) {
            this.path = path;
        }

        public Definition via(HttpMethod... methods) {
            return via(oneOf(methods));
        }

        public Definition via(Matcher<? super HttpMethod> method) {
            this.method = method;
            return this;
        }

        public Definition to(Application application) {
            this.app = application;
            return this;
        }

        public DynamicRoute toRoute() {
            return new DynamicRoute(path, method, app);
        }

        private Matcher<? super HttpMethod> oneOf(HttpMethod... methods) {
            List<Matcher<? super HttpMethod>> matchMethods = new ArrayList<Matcher<? super HttpMethod>>();
            for (HttpMethod httpMethod : methods) {
                matchMethods.add(equalTo(httpMethod));
            }
            return anyOf(matchMethods);
        }
    }
}