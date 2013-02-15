package com.vtence.molecule;

public interface Matcher<T> {

    boolean matches(T actual);
}
