package com.vtence.molecule.support;

import com.vtence.molecule.http.HttpMethod;
import com.vtence.molecule.Request;
import org.hamcrest.Matcher;

import static com.vtence.molecule.support.RequestAssertions.assertThat;

public class MockRequest extends Request {

    public MockRequest() {
        uri("/");
        path("/");
        protocol("HTTP/1.1");
        method(HttpMethod.GET);
    }

    public static MockRequest GET(String path) {
        MockRequest request = new MockRequest();
        request.path(path);
        request.method(HttpMethod.GET);
        return request;
    }

    public static MockRequest POST(String path) {
        MockRequest request = new MockRequest();
        request.path(path);
        request.method(HttpMethod.POST);
        return request;
    }

    public static MockRequest PUT(String path) {
        MockRequest request = new MockRequest();
        request.path(path);
        request.method(HttpMethod.PUT);
        return request;
    }

    public static MockRequest DELETE(String path) {
        MockRequest request = new MockRequest();
        request.path(path);
        request.method(HttpMethod.DELETE);
        return request;
    }

    public static MockRequest PATCH(String path) {
        MockRequest request = new MockRequest();
        request.path(path);
        request.method(HttpMethod.PATCH);
        return request;
    }

    public static Request HEAD(String path) {
        MockRequest request = new MockRequest();
        request.path(path);
        request.method(HttpMethod.HEAD);
        return request;
    }

    public static Request OPTIONS(String path) {
        MockRequest request = new MockRequest();
        request.path(path);
        request.method(HttpMethod.OPTIONS);
        return request;
    }

    /**
     * @see RequestAssertions#hasAttribute(Object, Matcher)
     */
    @Deprecated
    public void assertAttribute(Object key, Matcher<Object> attributeMatcher) {
        assertThat(this).hasAttribute(key, attributeMatcher);
    }

    public String toString() {
        return String.format("%s %s", method(), path());
    }
}