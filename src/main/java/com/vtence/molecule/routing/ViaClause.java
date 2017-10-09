package com.vtence.molecule.routing;

import com.vtence.molecule.http.HttpMethod;

import java.util.function.Predicate;

public interface ViaClause extends AcceptClause {

    AcceptClause via(HttpMethod... methods);

    AcceptClause via(Predicate<? super HttpMethod> method);
}