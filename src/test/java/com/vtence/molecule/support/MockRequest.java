package com.vtence.molecule.support;

import com.vtence.molecule.HttpMethod;
import com.vtence.molecule.simple.SimpleRequest;
import org.hamcrest.Matcher;

import static org.junit.Assert.assertThat;

public class MockRequest extends SimpleRequest {

    public MockRequest() {
        uri("/");
        path("/");
        protocol("HTTP/1.1");
        method(HttpMethod.GET);
    }

    public static MockRequest aRequest() {
        return new MockRequest();
    }

    public static MockRequest GET(String path) {
        return aRequest().path(path).method(HttpMethod.GET);
    }

    public static MockRequest POST(String path) {
        return aRequest().path(path).method(HttpMethod.POST);
    }

    public static MockRequest PUT(String path) {
        return aRequest().path(path).method(HttpMethod.PUT);
    }

    public static MockRequest DELETE(String path) {
        return aRequest().path(path).method(HttpMethod.DELETE);
    }

    public MockRequest path(String path) {
        return (MockRequest) super.path(path);
    }

    public MockRequest remoteIp(String ip) {
        return (MockRequest) super.remoteIp(ip);
    }

    public MockRequest method(HttpMethod method) {
        return (MockRequest) super.method(method);
    }

    public MockRequest addCookie(String name, String value) {
        return (MockRequest) super.addCookie(name, value);
    }

    public <T> T unwrap(Class<T> type) {
        throw new UnsupportedOperationException();
    }

    public void assertAttribute(Object key, Matcher<Object> attributeMatcher) {
        assertThat("attribute[" + key.toString() + "]", attribute(key), attributeMatcher);
    }

    public String toString() {
        return String.format("%s %s", method(), path());
    }
}