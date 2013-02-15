package com.vtence.molecule.routing;

import com.vtence.molecule.Application;
import com.vtence.molecule.HttpMethod;

// todo consider having separate clauses
public interface RouteDefinition {

    RouteDefinition map(String path);

    RouteDefinition via(HttpMethod method);

    RouteDefinition to(Application application);
}
