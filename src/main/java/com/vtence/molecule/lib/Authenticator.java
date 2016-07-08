package com.vtence.molecule.lib;

import java.util.Optional;

@FunctionalInterface
public interface Authenticator {

    Optional<String> authenticate(String... credentials);
}
