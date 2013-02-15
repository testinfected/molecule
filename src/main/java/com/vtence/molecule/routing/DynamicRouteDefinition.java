package com.vtence.molecule.routing;

import com.vtence.molecule.Application;
import com.vtence.molecule.HttpMethod;
import com.vtence.molecule.Matcher;
import com.vtence.molecule.matchers.IsEqual;
import com.vtence.molecule.matchers.Matchers;

public class DynamicRouteDefinition implements RouteDefinition {

    public static DynamicRouteDefinition route() {
        return new DynamicRouteDefinition();
    }

    private String path;
    private Matcher<? super String> method = Matchers.anyMethod();
    private Application app;

    public DynamicRouteDefinition map(String path) {
        this.path = path;
        return this;
    }

    public DynamicRouteDefinition via(HttpMethod method) {
        return via(IsEqual.equalTo(method.name()));
    }

    public DynamicRouteDefinition via(Matcher<? super String> method) {
        this.method = method;
        return this;
    }

    public DynamicRouteDefinition to(Application application) {
        this.app = application;
        return this;
    }

    public DynamicRoute draw() {
        checkValidity();
        return new DynamicRoute(path, method, app);
    }

    public void checkValidity() {
        if (path == null) throw new IllegalStateException("No path was specified");
    }
}
