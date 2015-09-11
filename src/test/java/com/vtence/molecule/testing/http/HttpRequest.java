package com.vtence.molecule.testing.http;

import com.vtence.molecule.helpers.Headers;
import com.vtence.molecule.helpers.Joiner;
import com.vtence.molecule.helpers.Streams;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.vtence.molecule.lib.SecureProtocol.TLS;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

public class HttpRequest {
    private final String host;
    private final int port;
    private final Headers headers = new Headers();
    private final Map<String, String> cookies = new LinkedHashMap<>();

    private String path = "/";
    private String method = "GET";
    private HttpContent content = new EmptyContent();
    private boolean followRedirects;
    private boolean secure;

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
        return header(name, asList(values));
    }

    public HttpRequest header(String name, Iterable<String> values) {
        headers.remove(name);
        for (String value : values) {
            headers.add(name, value);
        }
        return this;
    }

    public HttpRequest contentType(String contentType) {
        return header("Content-Type", contentType);
    }

    public HttpRequest cookie(String name, String value) {
        cookies.put(name, value);
        return this;
    }

    private String contentType() {
        return headers.get("Content-Type");
    }

    public HttpRequest body(String text) {
        this.content = new TextContent(text, contentType());
        return body(text.getBytes());
    }

    public HttpRequest body(byte[] content) {
        this.content = new BinaryContent(content, contentType());
        return this;
    }

    public HttpRequest content(HttpContent content) {
        this.content = content;
        contentType(content.contentType());
        return this;
    }

    public HttpRequest method(String method) {
        this.method = method;
        return this;
    }

    public HttpRequest followRedirects(boolean follow) {
        this.followRedirects = follow;
        return this;
    }

    public HttpRequest secure(boolean secure) {
        this.secure = secure;
        return this;
    }

    public HttpResponse get(String path) throws IOException {
        path(path);
        method("GET");
        return send();
    }

    public HttpResponse post(String path) throws IOException {
        path(path);
        method("POST");
        return send();
    }

    public HttpResponse put(String path) throws IOException {
        path(path);
        method("PUT");
        return send();
    }

    public HttpResponse delete(String path) throws IOException {
        path(path);
        method("DELETE");
        return send();
    }

    public HttpRequest but() {
        HttpRequest request = new HttpRequest(host, port);
        request.path(path);
        request.method(method);
        request.followRedirects(followRedirects);
        request.secure(secure);
        for (String header : headers.names()) {
            request.header(header, headers.list(header));
        }
        for (String cookie : cookies.keySet()) {
            request.cookie(cookie, cookies.get(cookie));
        }
        request.content(content);
        return request;
    }

    public HttpResponse send() throws IOException {
        HttpURLConnection connection = secure ? openSecureConnection() : openConnection();
        connection.setRequestMethod(method);
        addCookieHeader();
        setRequestHeaders(connection);
        writeContent(connection);
        connection.setInstanceFollowRedirects(followRedirects);
        connection.connect();

        int statusCode = connection.getResponseCode();
        String statusMessage = connection.getResponseMessage();
        Map<String, List<String>> headers = connection.getHeaderFields();
        byte[] body = readResponseBody(connection);
        connection.disconnect();

        return new HttpResponse(statusCode, statusMessage, headers, body);
    }

    private HttpURLConnection openConnection() throws IOException {
        return (HttpURLConnection) new URL("http", host, port, path).openConnection();
    }

    private HttpURLConnection openSecureConnection() throws IOException {
        HttpsURLConnection connection = (HttpsURLConnection) new URL("https", host, port, path).openConnection();
        SSLContext sslContext;
        try {
            sslContext = TLS.initialize(null, new TrustManager[] { Trust.allCertificates() });
        } catch (GeneralSecurityException e) {
            throw new AssertionError("Failed to setup  SSL", e);
        }
        connection.setSSLSocketFactory(sslContext.getSocketFactory());
        connection.setHostnameVerifier(Trust.allHostNames());
        return connection;
    }

    private byte[] readResponseBody(HttpURLConnection connection) throws IOException {
        InputStream bodyStream = successful(connection) ? connection.getInputStream() : connection.getErrorStream();
        return bodyStream != null ? Streams.toBytes(bodyStream) : new byte[0];
    }

    private boolean successful(HttpURLConnection connection) throws IOException {
        return connection.getResponseCode() < 400;
    }

    private void setRequestHeaders(HttpURLConnection connection) {
        for (String name : headers.names()) {
            for (String value: headers.list(name)) {
                connection.addRequestProperty(name, value);
            }
        }
    }

    private void addCookieHeader() {
        if (cookies.isEmpty()) return;
        List<String> pairs = cookies.keySet().stream().map(name -> name + "=" + cookies.get(name)).collect(toList());
        header("Cookie", Joiner.on("; ").join(pairs));
    }

    private void writeContent(HttpURLConnection connection) throws IOException {
        if (content.contentLength() > 0) {
            connection.setDoOutput(true);
            content.writeTo(connection.getOutputStream());
        }
    }
}