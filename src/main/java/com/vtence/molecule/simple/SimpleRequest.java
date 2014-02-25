package com.vtence.molecule.simple;

import com.vtence.molecule.HttpMethod;
import com.vtence.molecule.Session;
import com.vtence.molecule.simple.session.SessionTracking;
import org.simpleframework.http.Cookie;

import java.io.IOException;
import java.util.Map;

public class SimpleRequest implements com.vtence.molecule.Request {

    private final org.simpleframework.http.Request request;
    private final SessionTracking sessionTracking;

    public SimpleRequest(org.simpleframework.http.Request request, SessionTracking sessionTracking) {
        this.request = request;
        this.sessionTracking = sessionTracking;
    }

    public String protocol() {
        return String.format("HTTP/%s.%s", request.getMajor(), request.getMinor());
    }

    public String uri() {
        return request.getTarget();
    }

    public String pathInfo() {
        return request.getPath().getPath();
    }

    public String body() throws IOException {
        return request.getContent();
    }

    public HttpMethod method() {
        return HttpMethod.valueOf(request.getMethod());
    }

    public String parameter(String name) {
        return request.getParameter(name);
    }

    public String ip() {
        return request.getClientAddress().getAddress().getHostAddress();
    }

    public Object attribute(Object key) {
        return request.getAttribute(key);
    }

    @SuppressWarnings("unchecked")
    public void attribute(Object key, Object value) {
        request.getAttributes().put(key, value);
    }

    public void removeAttribute(Object key) {
        request.getAttributes().remove(key);
    }

    @SuppressWarnings("unchecked")
    public Map<Object, Object> attributes() {
        return request.getAttributes();
    }

    public String cookie(String name) {
        Cookie cookie = request.getCookie(name);
        return cookie != null ? cookie.getValue() : null;
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