package com.vtence.molecule.session;

import static java.util.UUID.randomUUID;

public class SecureIdentifierPolicy implements SessionIdentifierPolicy {

    public String generateId() {
        return randomUUID().toString();
    }
}