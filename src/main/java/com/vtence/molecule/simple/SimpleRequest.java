package com.vtence.molecule.simple;

import com.vtence.molecule.HttpMethod;
import com.vtence.molecule.Session;
import com.vtence.molecule.simple.session.SessionTracking;
import org.simpleframework.http.Cookie;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SimpleRequest implements com.vtence.molecule.Request {

    private final org.simpleframework.http.Request request;
    private final SessionTracking sessionTracking;

    public SimpleRequest(org.simpleframework.http.Request request, SessionTracking sessionTracking) {
        this.request = request;
        this.sessionTracking = sessionTracking;
    }

    public String body() throws IOException {
        return request.getContent();
    }

    public long contentLength() {
        return request.getContentLength();
    }

    public String contentType() {
        return request.getContentType().getType();
    }

    public Set<String> headerNames() {
        return new LinkedHashSet<String>(request.getNames());
    }

    public List<String> headers(String name) {
        return request.getValues(name);
    }

    public String header(String name) {
        return request.getValue(name);
    }

    public HttpMethod method() {
        return HttpMethod.valueOf(request.getMethod());
    }

    public String parameter(String name) {
        return request.getParameter(name);
    }

    public String[] parameters(String name) {
        List<String> values = request.getQuery().getAll(name);
        return values.toArray(new String[values.size()]);
    }

    public String uri() {
        return request.getTarget();
    }

    public String pathInfo() {
        return request.getPath().getPath();
    }

    public String ip() {
        return request.getClientAddress().getAddress().getHostAddress();
    }

    public String protocol() {
        return String.format("HTTP/%s.%s", request.getMajor(), request.getMinor());
    }

    public Map<String, String> cookies() {
        Map<String, String> cookies = new HashMap<String, String>();
        for (Cookie cookie: request.getCookies()) {
            cookies.put(cookie.getName(), cookie.getValue());
        }
        return cookies;
    }

    public String cookie(String name) {
        Cookie cookie = request.getCookie(name);
        return cookie != null ? cookie.getValue() : null;
    }

    public Object attribute(Object key) {
        return request.getAttribute(key);
    }

    @SuppressWarnings("unchecked")
    public Map<Object, Object> attributes() {
        return request.getAttributes();
    }

    @SuppressWarnings("unchecked")
    public void attribute(Object key, Object value) {
        request.getAttributes().put(key, value);
    }

    public void removeAttribute(Object key) {
        request.getAttributes().remove(key);
    }

    public Session session() {
        return session(true);
    }

    public Session session(boolean create) {
        return sessionTracking.openSession(this, create);
    }

    public <T> T unwrap(Class<T> type) {
        if (!type.isAssignableFrom(request.getClass()))
            throw new IllegalArgumentException("Unsupported type: " + type.getName());
        return type.cast(request);
    }
}