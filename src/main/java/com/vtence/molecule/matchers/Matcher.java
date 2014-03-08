package com.vtence.molecule.matchers;

public interface Matcher<T> {

    boolean matches(T actual);
}
