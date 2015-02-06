package com.vtence.molecule.test;

import com.vtence.molecule.helpers.Streams;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

public class HttpRequest {
    private final String host;
    private final int port;

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

    public HttpResponse send() throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL("http", host, port, path).openConnection();
        connection.connect();
        byte[] content = Streams.toBytes(connection.getInputStream());
        connection.disconnect();
        Map<String, List<String>> headers = connection.getHeaderFields();
        return new HttpResponse(connection.getResponseCode(), connection.getResponseMessage(), headers, content);
    }
}