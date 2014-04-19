package com.vtence.molecule.support;

import com.vtence.molecule.Cookie;
import com.vtence.molecule.HttpMethod;
import com.vtence.molecule.simple.SimpleRequest;
import org.hamcrest.Matcher;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertThat;

public class MockRequest extends SimpleRequest {

    private final Map<String, List<String>> headers = new HashMap<String, List<String>>();
    private final Map<String, List<String>> params = new HashMap<String, List<String>>();
    private final Map<Object, Object> attributes = new HashMap<Object, Object>();
    private final Map<String, Cookie> cookies = new HashMap<String, Cookie>();

    private HttpMethod method = HttpMethod.GET;

    public MockRequest() {
        uri("/");
        path("/");
        protocol("HTTP/1.1");
    }

    public static MockRequest aRequest() {
        return new MockRequest();
    }

    public static MockRequest GET(String path) {
        return aRequest().path(path).withMethod(HttpMethod.GET);
    }

    public static MockRequest POST(String path) {
        return aRequest().path(path).withMethod(HttpMethod.POST);
    }

    public static MockRequest PUT(String path) {
        return aRequest().path(path).withMethod(HttpMethod.PUT);
    }

    public static MockRequest DELETE(String path) {
        return aRequest().path(path).withMethod(HttpMethod.DELETE);
    }

    public MockRequest path(String path) {
        return (MockRequest) super.path(path);
    }

    public MockRequest remoteIp(String ip) {
        return (MockRequest) super.remoteIp(ip);
    }

    public MockRequest withMethod(HttpMethod method) {
        this.method = method;
        return this;
    }

    public long contentLength() {
        return -1;
    }

    public String contentType() {
        return null;
    }

    public MockRequest withHeader(String header, String... values) {
        headers.put(header, asList(values));
        return this;
    }

    public List<String> headerNames() {
        return new ArrayList<String>(headers.keySet());
    }

    public List<String> headers(String name) {
        List<String> values = headers.get(name);
        return values != null ? new ArrayList<String>(values) : new ArrayList<String>();
    }

    public String header(String name) {
        List<String> values = headers(name);
        return values.isEmpty() ? null : values.get(0);
    }

    public HttpMethod method() {
        return method;
    }

    public void addParameter(String name, String value) {
        if (!params.containsKey(name)) params.put(name, new ArrayList<String>());
        params.get(name).add(value);
    }

    public MockRequest withParameter(String name, String value) {
        addParameter(name, value);
        return this;
    }

    public String parameter(String name) {
        String[] values = parameters(name);
        if (values.length == 0) return null;
        return values[0];
    }

    public String[] parameters(String name) {
        List<String> values = params.get(name);
        if (values == null) return new String[0];
        return values.toArray(new String[values.size()]);
    }

    public String body() throws IOException {
        return null;
    }

    public MockRequest withCookie(String name, String value) {
        cookies.put(name, new Cookie(name, value));
        return this;
    }

    public List<Cookie> cookies() {
        return new ArrayList<Cookie>(cookies.values());
    }

    public Cookie cookie(String name) {
        return cookies.get(name);
    }

    @Override
    public String cookieValue(String name) {
        Cookie cookie = cookie(name);
        return cookie != null ? cookie.value() : null;
    }

    public <T> T unwrap(Class<T> type) {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("unchecked")
    public <T> T attribute(Object key) {
        return (T) attributes.get(key);
    }

    public void attribute(Object key, Object value) {
        attributes.put(key, value);
    }

    public void removeAttribute(Object key) {
        attributes.remove(key);
    }

    public Map<Object, Object> attributes() {
        return attributes;
    }

    public void assertAttribute(Object key, Matcher<Object> attributeMatcher) {
        assertThat("attribute[" + key.toString() + "]", attribute(key), attributeMatcher);
    }

    public String toString() {
        return String.format("%s %s", method(), path());
    }
}