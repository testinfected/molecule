package com.vtence.molecule;

import com.vtence.molecule.util.Charsets;
import com.vtence.molecule.util.ContentType;
import com.vtence.molecule.util.Headers;
import com.vtence.molecule.util.HttpDate;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.vtence.molecule.HttpHeaders.CONTENT_LENGTH;
import static com.vtence.molecule.HttpHeaders.CONTENT_TYPE;
import static com.vtence.molecule.TextBody.text;
import static java.lang.Long.parseLong;

public class Response {
    private final Headers headers = new Headers();
    private final Map<String, Cookie> cookies = new LinkedHashMap<String, Cookie>();

    private int statusCode = HttpStatus.OK.code;
    private String statusText = HttpStatus.OK.text;

    private Body body = BinaryBody.empty();

    public Response() {}

    public void status(HttpStatus status) {
        statusCode(status.code);
        statusText(status.text);
    }

    public void statusCode(int code) {
        statusCode = code;
    }

    public int statusCode() {
        return statusCode;
    }

    public void statusText(String text) {
        statusText = text;
    }

    public String statusText() {
        return statusText;
    }

    public void redirectTo(String location) {
        status(HttpStatus.SEE_OTHER);
        set(HttpHeaders.LOCATION, location);
    }

    public boolean has(String name) {
        return headers.has(name);
    }

    public String get(String name) {
        return headers.get(name);
    }

    public long getLong(String name) {
        String value = get(name);
        return value != null ? parseLong(value) : -1;
    }

    public void set(String name, String value) {
        headers.put(name, value);
    }

    public void setLong(String name, long value) {
        set(name, String.valueOf(value));
    }

    public void set(String name, Date date) {
        headers.put(name, HttpDate.format(date));
    }

    public void setDate(String name, long date) {
        set(name, new Date(date));
    }

    public void remove(String name) {
        headers.remove(name);
    }

    public Iterable<String> names() {
        return headers.names();
    }

    public Map<String, String> headers() {
        return headers.all();
    }

    public String contentType() {
        return get(CONTENT_TYPE);
    }

    public void contentType(String mediaType) {
        set(CONTENT_TYPE, mediaType);
    }

    public long contentLength() {
        return getLong(CONTENT_LENGTH);
    }

    public void contentLength(long length) {
        setLong(CONTENT_LENGTH, length);
    }

    public void add(Cookie cookie) {
        cookies.put(cookie.name(), cookie);
    }

    public Cookie cookie(String name) {
        return cookies.get(name);
    }

    public Iterable<Cookie> cookies() {
        return new ArrayList<Cookie>(cookies.values());
    }

    public Charset charset() {
        ContentType contentType = ContentType.of(this);
        if (contentType == null || contentType.charsetName() == null) {
            return Charsets.ISO_8859_1;
        }
        return contentType.charset();
    }

    public void body(String text) throws IOException {
        body(text(text));
    }

    public void body(Body body) throws IOException {
        this.body = body;
    }

    public Body body() {
        return body;
    }

    public long size() {
        return body.size(charset());
    }

    public boolean empty() {
        return size() == 0;
    }
}
