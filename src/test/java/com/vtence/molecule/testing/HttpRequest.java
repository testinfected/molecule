package com.vtence.molecule.testing;

import com.vtence.molecule.helpers.Charsets;
import com.vtence.molecule.helpers.Headers;
import com.vtence.molecule.helpers.Joiner;
import com.vtence.molecule.helpers.Streams;
import com.vtence.molecule.http.ContentType;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.vtence.molecule.lib.SecureProtocol.TLS;
import static java.util.Arrays.asList;

public class HttpRequest {
    private final String host;
    private final int port;
    private final Headers headers = new Headers();
    private final Map<String, String> cookies = new LinkedHashMap<String, String>();

    private String path = "/";
    private String method = "GET";
    private byte[] body = new byte[0];
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

    public HttpRequest body(String text) {
        return body(text.getBytes(charset()));
    }

    private Charset charset() {
        ContentType contentType = ContentType.parse(headers.get("Content-Type"));
        if (contentType == null || contentType.charset() == null) return Charsets.ISO_8859_1;

        return contentType.charset();
    }

    public HttpRequest body(byte[] content) {
        body = content;
        return this;
    }

    public HttpRequest body(HtmlForm form) {
        contentType(form.contentType());
        body(form.encode());
        return this;
    }

    public HttpRequest body(FormData form) throws IOException {
        contentType(form.contentType());
        body(form.encode());
        return this;
    }

    public HttpRequest body(FileUpload upload) throws IOException {
        String boundary = randomBoundary();
        contentType("multipart/form-data; boundary=" + boundary);
        body(upload.encode(boundary));
        return this;
    }

    private static String randomBoundary() {
        return Long.toHexString(System.currentTimeMillis());
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
        request.body(body);
        return request;
    }

    public HttpResponse send() throws IOException {
        HttpURLConnection connection = secure ? openSecureConnection() : openUnsecureConnection();
        connection.setRequestMethod(method);
        addCookieHeader();
        setRequestHeaders(connection);
        writeBody(connection);
        connection.setInstanceFollowRedirects(followRedirects);
        connection.connect();

        int statusCode = connection.getResponseCode();
        String statusMessage = connection.getResponseMessage();
        Map<String, List<String>> headers = connection.getHeaderFields();
        byte[] body = readResponseBody(connection);
        connection.disconnect();

        return new HttpResponse(statusCode, statusMessage, headers, body);
    }

    private HttpURLConnection openUnsecureConnection() throws IOException {
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
        return Streams.toBytes(successful(connection) ? connection.getInputStream() : connection.getErrorStream());
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
}