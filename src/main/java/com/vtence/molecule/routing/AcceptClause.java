package com.vtence.molecule.routing;

import java.util.function.Predicate;

public interface AcceptClause extends ToClause {

    ToClause accept(String mimeType);

    ToClause accept(Predicate<? super String> mimeType);
}