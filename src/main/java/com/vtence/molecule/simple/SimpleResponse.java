package com.vtence.molecule.simple;

import com.vtence.molecule.BinaryBody;
import com.vtence.molecule.Body;
import com.vtence.molecule.Cookie;
import com.vtence.molecule.HttpHeaders;
import com.vtence.molecule.HttpStatus;
import com.vtence.molecule.util.Charsets;
import com.vtence.molecule.util.Headers;
import com.vtence.molecule.util.HttpDate;
import org.simpleframework.http.ContentType;
import org.simpleframework.http.Response;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.Map;

import static com.vtence.molecule.HttpHeaders.CONTENT_LENGTH;
import static com.vtence.molecule.HttpHeaders.CONTENT_TYPE;
import static com.vtence.molecule.TextBody.text;
import static java.lang.Long.parseLong;

public class SimpleResponse implements com.vtence.molecule.Response {
    private final Headers headers = new Headers();

    private int statusCode = HttpStatus.OK.code;
    private String statusText = HttpStatus.OK.text;

    private final Response response;
    private Body body = BinaryBody.empty();

    public SimpleResponse(Response response) {
        this.response = response;
    }

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

    public void cookie(Cookie cookie) {
        org.simpleframework.http.Cookie cooky =
                new org.simpleframework.http.Cookie(cookie.name(), cookie.value(), true);
        cooky.setProtected(cookie.httpOnly());
        response.setCookie(cooky);
    }

    public Charset charset() {
        ContentType type = response.getContentType();

        if (type == null || type.getCharset() == null) {
            return Charsets.ISO_8859_1;
        }

        return Charset.forName(type.getCharset());
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

    public <T> T unwrap(Class<T> type) {
        if (!type.isAssignableFrom(response.getClass()))
            throw new IllegalArgumentException("Unsupported type: " + type.getName());
        return type.cast(response);
    }
}
