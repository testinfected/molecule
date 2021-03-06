package com.vtence.molecule.routing;

import org.hamcrest.Matchers;
import org.junit.Test;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasSize;

public class DynamicPathTest {

    @Test public void
    matchesIdenticalStaticPaths() {
        DynamicPath dynamicPath = DynamicPath.equalTo("/products");
        assertThat("no match", dynamicPath.test("/products"));
    }

    @Test public void
    rejectsDifferentStaticPaths() {
        DynamicPath dynamicPath = DynamicPath.equalTo("/products");
        assertThat("match", !dynamicPath.test("/items"));
    }

    @Test public void
    ignoresDynamicSegmentsWhenMatching() {
        DynamicPath dynamicPath = DynamicPath.equalTo("/products/:number/items/:id");
        assertThat("no match", dynamicPath.test("/products/LAB-1234/items/12345678"));
    }

    @Test public void
    expectsPathsWithExactlySameNumberOfSegments() {
        DynamicPath dynamicPath = DynamicPath.equalTo("/products/:number");
        assertThat("match", !dynamicPath.test("/products/LAB-1234/items/1234"));
    }

    @Test public void
    expectsPathsWithAtLeastSameNumberOfSegments() {
        DynamicPath dynamicPath = DynamicPath.startingWith("/products/:number");
        assertThat("match prefix", dynamicPath.test("/products/LAB-1234"));
        assertThat("match longer path", dynamicPath.test("/products/LAB-1234/items/1234"));
    }

    @Test public void
    staticPathsHaveNoBoundParameters() {
        DynamicPath dynamicPath = DynamicPath.equalTo("/products");
        Map<String, String> boundParameters = dynamicPath.parametersBoundTo("/products");
        assertThat("bound parameters values", boundParameters.values(), Matchers.<String>empty());
    }

    @Test public void
    extractBoundParametersFromDynamicSegments() {
        DynamicPath dynamicPath = DynamicPath.equalTo("/products/:number/items/:id");
        Map<String, String> boundParameters = dynamicPath.parametersBoundTo("/products/LAB-1234/items/12345678");
        assertThat("bound parameters values", boundParameters.values(), hasSize(2));
        assertThat("bound parameters", boundParameters, allOf(hasEntry("number", "LAB-1234"), hasEntry("id", "12345678")));
    }
}