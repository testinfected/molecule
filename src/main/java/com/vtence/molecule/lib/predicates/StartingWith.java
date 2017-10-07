package com.vtence.molecule.lib.predicates;

import java.util.function.Predicate;

public class StartingWith implements Predicate<String> {
    private final String prefix;

    public StartingWith(String prefix) {
        this.prefix = prefix;
    }

    public boolean test(String actual) {
        return actual.startsWith(prefix);
    }

    public static Predicate<String> startingWith(String prefix) {
        return new StartingWith(prefix);
    }
}
