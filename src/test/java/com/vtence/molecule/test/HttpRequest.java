package com.vtence.molecule.test;

import com.vtence.molecule.helpers.Joiner;
import com.vtence.molecule.helpers.Streams;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class HttpRequest {
    private final String host;
    private final int port;
    private final Map<String, List<String>> headers = new HashMap<String, List<String>>();
    private final Map<String, String> cookies = new LinkedHashMap<String, String>();

    private String path = "/";
    private String method = "GET";
    private Charset charset = Charset.forName("ISO-8859-1");
    private byte[] body = new byte[0];

    public HttpRequest(int port) {
        this("localhost", port);
    }

    public HttpRequest(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public HttpRequest path(String path) {
        this.path = path;
        return this;
    }

    public HttpRequest header(String name, String... values) {
        List<String> headers = headers(name);
        headers.clear();
        Collections.addAll(headers, values);
        return this;
    }

    public HttpRequest contentType(String contentType) {
        return header("Content-Type", contentType + "; charset=" + charset.name().toLowerCase());
    }

    public HttpRequest cookie(String name, String value) {
        cookies.put(name, value);
        return this;
    }

    public HttpRequest body(String text) {
        return body(text.getBytes(charset));
    }

    public HttpRequest body(byte[] content) {
        body = content;
        return this;
    }

    public HttpResponse get(String path) throws IOException {
        path(path);
        method = "GET";
        return send();
    }

    public HttpResponse post(String path) throws IOException {
        path(path);
        method = "POST";
        return send();
    }

    public HttpResponse send() throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL("http", host, port, path).openConnection();
        connection.setRequestMethod(method);
        addCookieHeader();
        setRequestHeaders(connection);
        writeBody(connection);
        connection.connect();

        int statusCode = connection.getResponseCode();
        String statusMessage = connection.getResponseMessage();
        Map<String, List<String>> headers = connection.getHeaderFields();
        byte[] content = Streams.toBytes(connection.getInputStream());
        connection.disconnect();

        return new HttpResponse(statusCode, statusMessage, headers, content);
    }

    private void setRequestHeaders(HttpURLConnection connection) {
        for (String name : headers.keySet()) {
            for (String value: headers(name)) {
                connection.addRequestProperty(name, value);
            }
        }
    }

    private void addCookieHeader() {
        if (cookies.isEmpty()) return;
        List<String> pairs = new ArrayList<String>();
        for (String name : cookies.keySet()) {
            pairs.add(name + "=" + cookies.get(name));
        }
        header("Cookie", Joiner.on("; ").join(pairs));
    }

    private void writeBody(HttpURLConnection connection) throws IOException {
        if (body.length > 0) {
            connection.setDoOutput(true);
            connection.getOutputStream().write(body);
        }
    }

    private List<String> headers(String name) {
        if (!headers.containsKey(name)) {
            headers.put(name, new ArrayList<String>());
        }
        return headers.get(name);
    }
}