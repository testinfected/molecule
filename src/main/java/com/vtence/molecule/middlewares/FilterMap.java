package com.vtence.molecule.middlewares;

import com.vtence.molecule.Application;
import com.vtence.molecule.Middleware;
import com.vtence.molecule.Request;
import com.vtence.molecule.Response;
import com.vtence.molecule.lib.matchers.Matcher;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.vtence.molecule.lib.matchers.Matchers.startingWith;
import static com.vtence.molecule.lib.matchers.Matchers.withPath;

public class FilterMap extends AbstractMiddleware {

    private final Map<Matcher<? super Request>, Middleware> filters = new LinkedHashMap<>();

    public Application then(Application next) {
        return Application.of(request -> filterFor(request).then(next).handle(request));
    }

    public void handle(Request request, Response response) throws Exception {
        Middleware filter = filterMappedTo(request);
        filter.connectTo(successor);
        filter.handle(request, response);
    }

    public FilterMap map(String pathPrefix, Middleware filter) {
        return map(withPath(startingWith(pathPrefix)), filter);
    }

    public FilterMap map(Matcher<? super Request> requestMatcher, Middleware filter) {
        filters.put(requestMatcher, filter);
        return this;
    }

    private Middleware filterFor(Request request) {
        return possibleMatches().stream()
                                .filter(m -> m.matches(request))
                                .findFirst()
                                .map(filters::get)
                                .orElse(Middleware.identity());
    }

    private List<Matcher<? super Request>> possibleMatches() {
        List<Matcher<? super Request>> matchers = new ArrayList<>(filters.keySet());
        Collections.reverse(matchers);
        return matchers;
    }

    private Middleware filterMappedTo(Request request) {
        Middleware bestMatch = new PassThrough();
        for (Matcher<? super Request> requestMatcher : filters.keySet()) {
            if (requestMatcher.matches(request)) bestMatch = filters.get(requestMatcher);
        }
        return bestMatch;
    }

    private static class PassThrough extends AbstractMiddleware {
        public void handle(Request request, Response response) throws Exception {
            successor.handle(request, response);
        }
    }
}