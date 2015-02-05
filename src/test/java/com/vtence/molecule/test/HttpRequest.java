package com.vtence.molecule.test;

import com.vtence.molecule.helpers.Streams;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpRequest {
    private final String host;
    private final int port;

    private final String path = "/";

    public HttpRequest(int port) {
        this("localhost", port);
    }

    public HttpRequest(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public HttpResponse send() throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL("http", host, port, path).openConnection();
        connection.connect();
        byte[] content = Streams.toBytes(connection.getInputStream());
        connection.disconnect();
        return new HttpResponse(content);
    }
}