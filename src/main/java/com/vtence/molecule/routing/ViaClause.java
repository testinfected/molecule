package com.vtence.molecule.routing;

import com.vtence.molecule.http.HttpMethod;

import java.util.function.Predicate;

public interface ViaClause extends ToClause {

    ToClause via(HttpMethod... methods);

    ToClause via(Predicate<? super HttpMethod> method);
}