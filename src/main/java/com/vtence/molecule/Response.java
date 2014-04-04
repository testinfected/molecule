package com.vtence.molecule;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Date;

public interface Response {

    void status(HttpStatus status);

    void statusCode(int code);

    int statusCode();

    void redirectTo(String location);

    boolean has(String name);

    String get(String name);

    void set(String name, String value);

    void set(String name, Date date);

    void setDate(String name, long date);

    void remove(String name);

    String contentType();

    void contentType(String contentType);

    long contentLength();

    void contentLength(long length);

    void cookie(Cookie cookie);

    Charset charset();

    void body(String text) throws IOException;

    void body(Body body) throws IOException;

    Body body();

    long size();

    boolean empty();

    <T> T unwrap(Class<T> type);
}
