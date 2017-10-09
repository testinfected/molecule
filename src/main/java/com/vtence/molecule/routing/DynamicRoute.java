package com.vtence.molecule.routing;

import com.vtence.molecule.Application;
import com.vtence.molecule.Request;

import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import static com.vtence.molecule.lib.predicates.Predicates.withPath;

public class DynamicRoute implements Route {

    private final Predicate<? super String> path;
    private final Predicate<? super Request> otherConditions;
    private final Application app;

    public DynamicRoute(Predicate<? super String> path,
                        Predicate<? super Request> otherConditions,
                        Application app) {
        this.path = path;
        this.otherConditions = otherConditions;
        this.app = app;
    }

    public Optional<Application> route(Request request) {
        return matches(request) ? Optional.of(extractPathParameters(app)) : Optional.empty();
    }

    private boolean matches(Request request) {
        return withPath(path).and(otherConditions).test(request);
    }

    private Application extractPathParameters(Application app) {
        return request -> {
            if (path instanceof WithBoundParameters) {
                WithBoundParameters dynamicPath = (WithBoundParameters) path;
                Map<String, String> dynamicParameters = dynamicPath.parametersBoundTo(request.path());
                for (String name : dynamicParameters.keySet()) {
                    request.addParameter(name, dynamicParameters.get(name));
                }
            }
            return app.handle(request);
        };
    }
}