package com.vtence.molecule.lib.matchers;

public interface Matcher<T> {

    boolean matches(T actual);
}
