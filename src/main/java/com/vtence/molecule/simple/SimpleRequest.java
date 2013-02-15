package com.vtence.molecule.simple;

import com.vtence.molecule.HttpException;
import com.vtence.molecule.HttpMethod;
import com.vtence.molecule.Request;
import com.vtence.molecule.Session;
import org.simpleframework.util.lease.LeaseException;

import java.io.IOException;
import java.util.Map;

public class SimpleRequest implements Request {

    private final org.simpleframework.http.Request request;

    public SimpleRequest(org.simpleframework.http.Request request) {
        this.request = request;
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

    public HttpMethod method() {
        return HttpMethod.valueOf(request.getMethod());
    }

    public String parameter(String name) {
        try {
            return request.getParameter(name);
        } catch (IOException e) {
            throw new HttpException("Cannot read request parameter", e);
        }
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

    public Session session() {
        try {
            return new SimpleSession(request.getSession());
        } catch (LeaseException e) {
            throw new HttpException("Cannot acquire session", e);
        }
    }

    public <T> T unwrap(Class<T> type) {
        if (!type.isAssignableFrom(request.getClass())) throw new IllegalArgumentException("Unsupported type: " + type.getName());
        return type.cast(request);
    }

    @SuppressWarnings("unchecked")
    public Map<Object, Object> attributes() {
        return request.getAttributes();
    }
}
