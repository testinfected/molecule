package com.vtence.molecule.http;

import org.junit.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

public class HeaderTest {

    @Test public void
    parsesAndSortsValuesInQualityOrder() {
        Header header = new Header("foo; q=0.5, bar, baz; q=0.9, qux, *; q=0");
        assertThat("header", header.toString(),
                equalTo("bar, qux, baz; q=0.9, foo; q=0.5, *; q=0"));
    }

    @Test public void
    parsesParametersAsAttributeValuePairs() {
        Header header = new Header("foo; q=0.5; bar; baz= ; qux");
        assertThat("header", header.toString(),
                equalTo("foo; q=0.5; bar; baz; qux"));
    }

    @Test public void
    ignoresLeadingAndTrailingWhitespace() {
        Header header = new Header("  foo,   bar ;   q  =  0.9   ");
        assertThat("header", header.toString(), equalTo("foo, bar; q=0.9"));
    }

    @Test public void
    recognizesQuotedStringsInValues() {
        Header header = new Header("\"foo, bar\"; q=0.8, baz, \"qux; q=0.6\"; q=0.6");
        assertThat("header", header.toString(),
                equalTo("baz, \"foo, bar\"; q=0.8, \"qux; q=0.6\"; q=0.6"));
    }

    @Test public void
    recognizesQuotedStringsInParameters() {
        Header header = new Header("foo; q=0.8, bar; \"q=0.5\"; \", baz; q=0.8\"");
        assertThat("header", header.toString(),
                equalTo("bar; \"q=0.5\"; \", baz; q=0.8\", foo; q=0.8"));
    }

    @Test public void
    listAcceptableValues() {
        Header header = new Header("foo, bar; q=0.8, baz, qux; q=0");
        assertThat("acceptable values", header.values(), contains("foo", "baz", "bar"));
    }

    @Test public void
    ignoresQualityIfNotFirstParameterOrNotANumber() {
        Header header = new Header("foo; bar; q=0, baz; q=0.8, qux; q=_");
        assertThat("acceptable values", header.values(), contains("foo", "qux", "baz"));
    }

    @Test public void
    recognizesParametersWithoutAnyValue() {
        Header header = new Header("for=192.0.2.60;proto=http;by=203.0.113.43");
        assertThat("is a single value", header.all(), hasSize(1));
        assertThat("without value", header.first().value(), emptyString());
        assertThat("made of 3 parameters", header.first().parameters(), hasSize(3));
    }

    @Test public void
    ignoresParameterNameCasing() {
        Header header = new Header("For=192.0.2.60");
        assertThat(header.first().parameter("for"), equalTo("192.0.2.60"));
    }
}