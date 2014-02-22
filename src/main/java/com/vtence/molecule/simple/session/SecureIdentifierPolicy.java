package com.vtence.molecule.simple.session;

import java.util.UUID;

public class SecureIdentifierPolicy implements SessionIdentifierPolicy {

    public String generateId() {
        return UUID.randomUUID().toString();
    }
}
