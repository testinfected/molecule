package com.vtence.molecule.session;

public interface SessionIdentifierPolicy {

    String generateId(Session data);
}