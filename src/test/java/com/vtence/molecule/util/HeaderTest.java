package com.vtence.molecule.util;

import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;

public class HeaderTest {

    @SuppressWarnings("unchecked") @Test public void
    parsesAndSortsEntriesInQualityOrder() {
        Header header = new Header("foo; q=0.5, bar, baz; q=0.9, qux, *;q=0");
        assertThat("sorted values", header.entries(), contains(
                value("bar", 1),
                value("qux", 1),
                value("baz", 0.9),
                value("foo", 0.5),
                value("*", 0)
        ));
    }

    @SuppressWarnings("unchecked") @Test public void
    handlesQuotedValues() {
        Header header = new Header("\"foo, bar\"; q=0.8, baz");
        assertThat("all values", header.entries(), contains(
                value("baz", 1),
                value("\"foo, bar\"", 0.8)
        ));
    }

    @SuppressWarnings("unchecked") @Test public void
    listAcceptableValues() {
        Header header = new Header("foo, bar; q=0.8, baz, qux; q=0");
        assertThat("acceptable values", header.values(), contains("foo", "baz", "bar"));
    }

    private Matcher<Header.Entry> value(String value, double quality) {
        return allOf(hasValue(value), hasQuality(quality));
    }

    private Matcher<Header.Entry> hasValue(String value) {
        return new FeatureMatcher<Header.Entry, String>(equalTo(value), "has value",
                "value") {

            protected String featureValueOf(Header.Entry actual) {
                return actual.value();
            }
        };
    }

    private Matcher<Header.Entry> hasQuality(double quality) {
        return new FeatureMatcher<Header.Entry, Double>(equalTo(quality), "has quality",
                "quality") {

            protected Double featureValueOf(Header.Entry actual) {
                return actual.quality();
            }
        };
    }
}
