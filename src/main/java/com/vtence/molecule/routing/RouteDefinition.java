package com.vtence.molecule.routing;

import com.vtence.molecule.matchers.Matcher;

public interface RouteDefinition {

    ViaClause map(String path);

    ViaClause map(Matcher<? super String> path);
}
