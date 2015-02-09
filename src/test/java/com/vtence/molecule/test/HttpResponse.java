package com.vtence.molecule.test;

import com.vtence.molecule.helpers.Joiner;

import java.net.HttpCookie;
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
        return Joiner.on(",").join(headers(name));
    }

    public List<String> headers(String name) {
        List<String> values = headers.get(name);
        return values != null ? values : new ArrayList<String>();
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
        // This is a simplification, we need to read the charset from the Content-Type header or default
        // to ISO-8859-1
        return new String(content);
    }

    public byte[] body() {
        return content;
    }
}