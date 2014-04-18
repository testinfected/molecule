package com.vtence.molecule.util;

import com.vtence.molecule.Cookie;
import com.vtence.molecule.HttpMethod;
import com.vtence.molecule.Request;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class RequestWrapper implements Request {
    protected final Request request;

    public RequestWrapper(Request request) {
        this.request = request;
    }

    public String body() throws IOException {
        return request.body();
    }

    public long contentLength() {
        return request.contentLength();
    }

    public String contentType() {
        return request.contentType();
    }

    public List<String> headerNames() {
        return request.headerNames();
    }

    public List<String> headers(String name) {
        return request.headers(name);
    }

    public String header(String name) {
        return request.header(name);
    }

    public HttpMethod method() {
        return request.method();
    }

    public String parameter(String name) {
        return request.parameter(name);
    }

    public String[] parameters(String name) {
        return request.parameters(name);
    }

    public String uri() {
        return request.uri();
    }

    public String pathInfo() {
        return request.pathInfo();
    }

    public String ip() {
        return request.ip();
    }

    public String protocol() {
        return request.protocol();
    }

    public Cookie cookie(String name) {
        return request.cookie(name);
    }

    public String cookieValue(String name) {
        return request.cookieValue(name);
    }

    public List<Cookie> cookies() {
        return request.cookies();
    }

    public <T> T attribute(Object key) {
        return request.attribute(key);
    }

    public Map<Object, Object> attributes() {
        return request.attributes();
    }

    public void attribute(Object key, Object value) {
        request.attribute(key, value);
    }

    public void removeAttribute(Object key) {
        request.removeAttribute(key);
    }

    public <T> T unwrap(Class<T> type) {
        return request.unwrap(type);
    }
}