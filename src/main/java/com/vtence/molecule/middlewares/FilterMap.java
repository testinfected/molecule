package com.vtence.molecule.middlewares;

import com.vtence.molecule.Application;
import com.vtence.molecule.Middleware;
import com.vtence.molecule.Request;
import com.vtence.molecule.routing.DynamicPath;
import com.vtence.molecule.routing.WithBoundParameters;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.vtence.molecule.Middleware.identity;
import static com.vtence.molecule.lib.predicates.Requests.withDynamicPath;

public class FilterMap implements Middleware {

    private final Map<Predicate<? super Request>, Middleware> filters = new LinkedHashMap<>();

    public Application then(Application next) {
        return request -> filterFor(request).then(next).handle(request);
    }

    public FilterMap map(String pathPrefix, Middleware filter) {
        return map(withDynamicPath(DynamicPath.startingWith(pathPrefix)), filter);
    }

    public FilterMap map(Predicate<? super Request> request, Middleware filter) {
        filters.put(request, filter);
        return this;
    }

    private Middleware filterFor(Request request) {
        return possibleMatches().stream()
                                .filter(m -> m.test(request))
                                .findFirst()
                                .map(addDynamicParametersTo(request))
                                .map(filters::get)
                                .orElse(identity());
    }

    private Function<Predicate<? super Request>, Predicate<? super Request>> addDynamicParametersTo(Request request) {
        return path -> {
            if (path instanceof WithBoundParameters) {
                var dynamicPath = (WithBoundParameters) path;
                dynamicPath.addParametersTo(request);
            }
            return path;
        };
    }

    private List<Predicate<? super Request>> possibleMatches() {
        var matchers = new ArrayList<>(filters.keySet());
        Collections.reverse(matchers);
        return matchers;
    }
}