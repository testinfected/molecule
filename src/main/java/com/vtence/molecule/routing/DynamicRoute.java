package com.vtence.molecule.routing;

import com.vtence.molecule.Application;
import com.vtence.molecule.Matcher;
import com.vtence.molecule.Request;
import com.vtence.molecule.Response;
import com.vtence.molecule.matchers.Combination;
import com.vtence.molecule.matchers.Matchers;
import com.vtence.molecule.util.RequestWrapper;

import java.util.Map;

public class DynamicRoute implements Route {

    private final DynamicPath path;
    private final Matcher<? super String> method;
    private final Application app;

    public DynamicRoute(String pathPattern, Matcher<? super String> method, Application app) {
        this.path = new DynamicPath(pathPattern);
        this.method = method;
        this.app = app;
    }

    public boolean matches(Request request) {
        return both(Matchers.withMethod(method)).and(Matchers.withPath(path)).matches(request);
    }

    private Combination<Request> both(Matcher<Request> matcher) {
        return Combination.both(matcher);
    }

    public void handle(Request request, Response response) throws Exception {
        app.handle(new BoundParameters(request), response);
    }

    public class BoundParameters extends RequestWrapper {
        private final Map<String, String> boundParameters;

        public BoundParameters(Request request) {
            super(request);
            boundParameters = path.boundParameters(request.pathInfo());
        }

        public String parameter(String name) {
            if (boundParameters.containsKey(name)) return boundParameters.get(name);
            else return super.parameter(name);
        }
    }
}
