package com.vtence.molecule.testing.http;

import com.vtence.molecule.helpers.Charsets;
import com.vtence.molecule.helpers.Joiner;
import com.vtence.molecule.http.ContentType;

import java.net.HttpCookie;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HttpResponse {
    private final int statusCode;
    private final String statusMessage;
    private final Map<String, List<String>> headers;
    private final byte[] content;

    public HttpResponse(int statusCode, String statusMessage, Map<String, List<String>> headers, byte[] content) {
        this.statusCode = statusCode;
        this.statusMessage = statusMessage;
        this.headers = headers;
        this.content = content;
    }

    public int statusCode() {
        return statusCode;
    }

    public String statusMessage() {
        return statusMessage;
    }

    public String header(String name) {
        List<String> headers = headers(name);
        if (headers.isEmpty()) return null;
        return Joiner.on(",").join(headers);
    }

    public List<String> headers(String name) {
        List<String> values = headers.get(name);
        return values != null ? values : new ArrayList<String>();
    }

    public String contentType() {
        return header("Content-Type");
    }

    public Charset charset() {
        ContentType contentType = ContentType.parse(contentType());
        if (contentType == null || contentType.charset() == null)
            return Charsets.ISO_8859_1;

        return contentType.charset();
    }

    public Map<String, HttpCookie> cookies() {
        Map<String, HttpCookie> cookies = new HashMap<String, HttpCookie>();
        for (String header : headers("Set-Cookie")) {
            for (HttpCookie cookie : HttpCookie.parse(header)) {
                cookies.put(cookie.getName(), cookie);
            }
        }
        return cookies;
    }

    public HttpCookie cookie(String name) {
        return cookies().get(name);
    }

    public String bodyText() {
        return new String(content, charset());
    }

    public byte[] body() {
        return content;
    }
}