package com.vtence.molecule.routing;

import com.vtence.molecule.HttpMethod;
import com.vtence.molecule.Matcher;

public interface ViaClause extends ToClause {

    ToClause via(HttpMethod... methods);

    ToClause via(Matcher<? super HttpMethod> method);
}
