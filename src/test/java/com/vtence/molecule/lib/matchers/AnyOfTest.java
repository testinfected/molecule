package com.vtence.molecule.lib.matchers;

import org.junit.Test;

import static com.vtence.molecule.lib.matchers.Matchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class AnyOfTest {

    @SuppressWarnings("unchecked") @Test public void
    evaluatesToLogicalDisjunctionOfMultipleMatchers() {
        Matcher<String> anyOf = Matchers.anyOf(equalTo("one"), equalTo("two"), equalTo("three"));

        assertThat("1st matches", anyOf.matches("one"), is(true));
        assertThat("2nd matches", anyOf.matches("two"), is(true));
        assertThat("3rd matches", anyOf.matches("three"), is(true));
        assertThat("none matches", anyOf.matches("five"), is(false));
    }

    @SuppressWarnings("unchecked") @Test public void
    matchesDescendantType() {
        Matcher<String> anyOf = Matchers.anyOf(new Anything<Object>());

        assertThat("matches", anyOf.matches("good"), is(true));
    }
}