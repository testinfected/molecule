package com.vtence.molecule.simple;

import com.vtence.molecule.Cookie;
import com.vtence.molecule.HttpHeaders;
import com.vtence.molecule.HttpMethod;
import com.vtence.molecule.Request;
import com.vtence.molecule.util.Charsets;
import com.vtence.molecule.util.ContentType;
import com.vtence.molecule.util.Headers;
import com.vtence.molecule.util.Streams;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.Long.parseLong;

public class SimpleRequest implements com.vtence.molecule.Request {

    private org.simpleframework.http.Request request;

    private final Headers headers = new Headers();
    private final Map<String, Cookie> cookies = new HashMap<String, Cookie>();
    private final Map<String, List<String>> parameters = new HashMap<String, List<String>>();
    private final Map<Object, Object> attributes = new HashMap<Object, Object>();

    private String uri;
    private String path;
    private String ip;
    private int port;
    private String hostName;
    private String protocol;
    private InputStream input;
    private HttpMethod method;

    public SimpleRequest() {
    }

    public SimpleRequest(org.simpleframework.http.Request request) {
        this.request = request;
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

    public Request remoteIp(String ip) {
        this.ip = ip;
        return this;
    }

    public String remoteIp() {
        return ip;
    }

    public Request remoteHost(String hostName) {
        this.hostName = hostName;
        return this;
    }

    public String remoteHost() {
        return hostName;
    }

    public Request remotePort(int port) {
        this.port = port;
        return this;
    }

    public int remotePort() {
        return port;
    }

    public Request protocol(String protocol) {
        this.protocol = protocol;
        return this;
    }

    public String protocol() {
        return protocol;
    }

    public Request method(String method) {
        return method(HttpMethod.valueOf(method));
    }

    public Request method(HttpMethod method) {
        this.method = method;
        return this;
    }

    public HttpMethod method() {
        return method;
    }

    public Request input(String body) {
        return input(body.getBytes(charset()));
    }

    public Request input(byte[] content) {
        this.input = new ByteArrayInputStream(content);
        return this;
    }

    public Request input(InputStream input) {
        this.input = input;
        return this;
    }

    public InputStream input() {
        return input;
    }

    public String body() throws IOException {
        return Streams.toString(input, charset());
    }

    public Charset charset() {
        ContentType contentType = ContentType.of(this);
        if (contentType == null || contentType.charset() == null) {
            return Charsets.ISO_8859_1;
        }
        return contentType.charset();
    }

    public Request addHeader(String name, String value) {
        headers.add(name, value);
        return this;
    }

    public Request header(String name, String value) {
        headers.put(name, value);
        return this;
    }

    public List<String> headerNames() {
        return headers.names();
    }

    public List<String> headers(String name) {
        return headers.list(name);
    }

    public String header(String name) {
        return headers.get(name);
    }

    public Request addCookie(String name, String value) {
        cookies.put(name, new Cookie(name, value));
        return this;
    }

    public List<Cookie> cookies() {
        return new ArrayList<Cookie>(cookies.values());
    }

    public Cookie cookie(String name) {
        return cookies.get(name);
    }

    public String cookieValue(String name) {
        Cookie cookie = cookie(name);
        return cookie != null ? cookie.value() : null;
    }

    public long contentLength() {
        String value = header(HttpHeaders.CONTENT_LENGTH);
        return value != null ? parseLong(value) : -1;
    }

    public String contentType() {
        ContentType contentType = ContentType.of(this);
        return contentType != null ? contentType.mediaType() : null;
    }

    public Request addParameter(String name, String value) {
        if (!parameters.containsKey(name)) {
            parameters.put(name, new ArrayList<String>());
        }
        parameters.get(name).add(value);
        return this;
    }

    public String parameter(String name) {
        List<String> values = parameters(name);
        return parameters.isEmpty() ?  null : values.get(values.size() - 1);
    }

    public List<String> parameters(String name) {
        return parameters.containsKey(name) ? new ArrayList<String>(parameters.get(name)) : new ArrayList<String>();
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

    public <T> T unwrap(Class<T> type) {
        if (!type.isAssignableFrom(request.getClass()))
            throw new IllegalArgumentException("Unsupported type: " + type.getName());
        return type.cast(request);
    }
}