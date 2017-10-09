package com.vtence.molecule.middlewares;

import com.vtence.molecule.Application;
import com.vtence.molecule.Middleware;
import com.vtence.molecule.Request;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import static com.vtence.molecule.Middleware.identity;
import static com.vtence.molecule.lib.predicates.Predicates.withPathPrefix;

public class FilterMap implements Middleware {

    private final Map<Predicate<? super Request>, Middleware> filters = new LinkedHashMap<>();

    public Application then(Application next) {
        return request -> filterFor(request).then(next).handle(request);
    }

    public FilterMap map(String pathPrefix, Middleware filter) {
        return map(withPathPrefix(pathPrefix), filter);
    }

    public FilterMap map(Predicate<? super Request> request, Middleware filter) {
        filters.put(request, filter);
        return this;
    }

    private Middleware filterFor(Request request) {
        return possibleMatches().stream()
                                .filter(m -> m.test(request))
                                .findFirst()
                                .map(filters::get)
                                .orElse(identity());
    }

    private List<Predicate<? super Request>> possibleMatches() {
        List<Predicate<? super Request>> matchers = new ArrayList<>(filters.keySet());
        Collections.reverse(matchers);
        return matchers;
    }
}