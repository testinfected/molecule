package com.vtence.molecule.routing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Predicate;

import static com.vtence.molecule.http.HttpMethod.DELETE;
import static com.vtence.molecule.http.HttpMethod.GET;
import static com.vtence.molecule.http.HttpMethod.HEAD;
import static com.vtence.molecule.http.HttpMethod.OPTIONS;
import static com.vtence.molecule.http.HttpMethod.PATCH;
import static com.vtence.molecule.http.HttpMethod.POST;
import static com.vtence.molecule.http.HttpMethod.PUT;

public class DynamicRoutes implements RouteBuilder {

    private final Collection<RouteDefinition> routes = new ArrayList<>();

    public void build(RouteSet routeSet) {
        for (RouteDefinition definition : this.routes) {
            routeSet.add(definition.toRoute());
        }
    }

    public ViaClause map(String path) {
        return openRoute(new DynamicPath(path));
    }

    public ViaClause map(Predicate<? super String> path) {
        return openRoute(path);
    }

    public ToClause get(String path) {
        return get(new DynamicPath(path));
    }

    public ToClause get(Predicate<? super String> path) {
        return map(path).via(GET);
    }

    public ToClause post(String path) {
        return post(new DynamicPath(path));
    }

    public ToClause post(Predicate<? super String> path) {
        return map(path).via(POST);
    }

    public ToClause put(String path) {
        return put(new DynamicPath(path));
    }

    public ToClause put(Predicate<? super String> path) {
        return map(path).via(PUT);
    }

    public ToClause delete(String path) {
        return delete(new DynamicPath(path));
    }

    public ToClause delete(Predicate<? super String> path) {
        return map(path).via(DELETE);
    }

    public ToClause patch(String path) {
        return patch(new DynamicPath(path));
    }

    public ToClause patch(Predicate<? super String> path) {
        return map(path).via(PATCH);
    }

    public ToClause head(String path) {
        return head(new DynamicPath(path));
    }

    public ToClause head(Predicate<? super String> path) {
        return map(path).via(HEAD);
    }

    public ToClause options(String path) {
        return options(new DynamicPath(path));
    }

    public ToClause options(Predicate<? super String> path) {
        return map(path).via(OPTIONS);
    }

    private RouteDefinition openRoute(Predicate<? super String> path) {
        RouteDefinition definition = RouteDefinition.map(path);
        routes.add(definition);
        return definition;
    }
}