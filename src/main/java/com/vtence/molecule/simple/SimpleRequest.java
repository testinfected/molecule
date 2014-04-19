package com.vtence.molecule.simple;

import com.vtence.molecule.Cookie;
import com.vtence.molecule.HttpMethod;
import com.vtence.molecule.Request;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SimpleRequest implements com.vtence.molecule.Request {

    private org.simpleframework.http.Request request;
    private String uri;
    private String path;
    private String ip;
    private int port;
    private String hostName;
    private String protocol;

    public SimpleRequest() {
    }

    public SimpleRequest(org.simpleframework.http.Request request) {
        this.request = request;
        // todo that's only temporary, until we no longer need the wrapped request
        uri(request.getTarget());
        path(request.getPath().getPath());
        remoteIp(request.getClientAddress().getAddress().getHostAddress());
        remotePort(request.getClientAddress().getPort());
        remoteHost(request.getClientAddress().getHostName());
        protocol(String.format("HTTP/%s.%s", request.getMajor(), request.getMinor()));
    }

    public Request uri(String uri) {
        this.uri = uri;
        return this;
    }

    public String uri() {
        return uri;
    }

    public Request path(String path) {
        this.path = path;
        return this;
    }

    public String path() {
        return path;
    }

    public String remoteIp() {
        return ip;
    }

    public Request remoteIp(String ip) {
        this.ip = ip;
        return this;
    }

    public String remoteHost() {
        return hostName;
    }

    public Request remoteHost(String hostName) {
        this.hostName = hostName;
        return this;
    }

    public int remotePort() {
        return port;
    }

    public Request remotePort(int port) {
        this.port = port;
        return this;
    }

    public String protocol() {
        return protocol;
    }

    public Request protocol(String protocol) {
        this.protocol = protocol;
        return this;
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

    public List<String> headerNames() {
        return request.getNames();
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

    public List<Cookie> cookies() {
        List<Cookie> cookies = new ArrayList<Cookie>();
        for (org.simpleframework.http.Cookie cookie: request.getCookies()) {
            cookies.add(new Cookie(cookie.getName(), cookie.getValue()));
        }
        return cookies;
    }

    public Cookie cookie(String name) {
        org.simpleframework.http.Cookie cooky = request.getCookie(name);
        if (cooky == null) return null;
        return new Cookie(cooky.getName(), cooky.getValue());
    }

    public String cookieValue(String name) {
        Cookie cookie = cookie(name);
        return cookie != null ? cookie.value() : null;
    }

    @SuppressWarnings("unchecked")
    public <T> T attribute(Object key) {
        return (T) request.getAttribute(key);
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

    public <T> T unwrap(Class<T> type) {
        if (!type.isAssignableFrom(request.getClass()))
            throw new IllegalArgumentException("Unsupported type: " + type.getName());
        return type.cast(request);
    }
}