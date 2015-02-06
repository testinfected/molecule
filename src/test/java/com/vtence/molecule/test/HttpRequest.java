package com.vtence.molecule.test;

import com.vtence.molecule.helpers.Streams;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HttpRequest {
    private final String host;
    private final int port;
    private final Map<String, List<String>> headers = new HashMap<String, List<String>>();

    private String path = "/";

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

    public HttpResponse get(String path) throws IOException {
        path(path);
        return send();
    }

    public HttpRequest header(String name, String... values) {
        List<String> headers = headers(name);
        headers.clear();
        Collections.addAll(headers, values);
        return this;
    }

    public HttpResponse send() throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL("http", host, port, path).openConnection();
        setRequestHeaders(connection);
        connection.connect();

        int statusCode = connection.getResponseCode();
        String statusMessage = connection.getResponseMessage();
        Map<String, List<String>> headers = connection.getHeaderFields();
        byte[] content = Streams.toBytes(connection.getInputStream());
        connection.disconnect();

        return new HttpResponse(statusCode, statusMessage, headers, content);
    }

    private List<String> headers(String name) {
        if (!headers.containsKey(name)) {
            headers.put(name, new ArrayList<String>());
        }
        return headers.get(name);
    }

    private void setRequestHeaders(HttpURLConnection connection) {
        for (String name : headers.keySet()) {
            for (String value: headers(name)) {
                connection.addRequestProperty(name, value);
            }
        }
    }
}