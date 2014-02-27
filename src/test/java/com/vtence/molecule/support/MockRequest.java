package com.vtence.molecule.support;

import org.hamcrest.Matcher;
import com.vtence.molecule.HttpMethod;
import com.vtence.molecule.Request;
import com.vtence.molecule.Session;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertThat;

public class MockRequest implements Request {

    private final Map<String, String> params = new HashMap<String, String>();
    private final Map<Object, Object> attributes = new HashMap<Object, Object>();
    private final Map<String, String> cookies = new HashMap<String, String>();
    private HttpMethod method = HttpMethod.GET;
    private String path = "/";
    private String ip = "127.0.0.1";
    private String protocol = "HTTP/1.1";
    private Session session;

    public static MockRequest aRequest() {
        return new MockRequest();
    }

    public static MockRequest GET(String path) {
        return aRequest().withPath(path).withMethod(HttpMethod.GET);
    }

    public static MockRequest POST(String path) {
        return aRequest().withPath(path).withMethod(HttpMethod.POST);
    }

    public static MockRequest PUT(String path) {
        return aRequest().withPath(path).withMethod(HttpMethod.PUT);
    }

    public static MockRequest DELETE(String path) {
        return aRequest().withPath(path).withMethod(HttpMethod.DELETE);
    }

    public MockRequest() {}

    public void setPath(String path) {
        withPath(path);
    }

    public MockRequest withPath(String path) {
        this.path = path;
        return this;
    }

    public MockRequest withMethod(HttpMethod method) {
        this.method = method;
        return this;
    }

    public MockRequest withIp(String address) {
        this.ip = address;
        return this;
    }

    public MockRequest withProtocol(String protocol) {
        this.protocol = protocol;
        return this;
    }

    public long contentLength() {
        return -1;
    }

    public String contentType() {
        return null;
    }

    public List<String> headers() {
        return null;
    }

    public List<String> headers(String name) {
        return null;
    }

    public String header(String name) {
        return null;
    }

    public HttpMethod method() {
        return method;
    }

    public String pathInfo() {
        return path;
    }

    public void addParameter(String name, String value) {
        params.put(name, value);
    }

    public MockRequest withParameter(String name, String value) {
        addParameter(name, value);
        return this;
    }

    public String parameter(String name) {
        return params.get(name);
    }

    public String protocol() {
        return protocol;
    }

    public String uri() {
        return pathInfo();
    }

    public String body() throws IOException {
        return null;
    }

    public <T> T unwrap(Class<T> type) {
        throw new UnsupportedOperationException();
    }

    public String ip() {
        return ip;
    }

    public Object attribute(Object key) {
        return attributes.get(key);
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

    public void withCookie(String name, String value) {
        cookies.put(name, value);
    }

    public String cookie(String name) {
        return cookies.get(name);
    }

    public Session session() {
        return session(true);
    }

    public Session session(boolean create) {
        if (session == null && create) {
            session = new MockSession();
        }
        return session;
    }

    public String toString() {
        return String.format("%s %s", method(), pathInfo());
    }
}