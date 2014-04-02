package com.vtence.molecule;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

public interface Response {

    void redirectTo(String location);

    String header(String name);

    void header(String name, String value);

    void headerDate(String name, long date);

    void removeHeader(String name);

    void cookie(Cookie cookie);

    void contentType(String contentType);

    String contentType();

    int statusCode();

    void status(HttpStatus status);

    void statusCode(int code);

    long contentLength();

    void contentLength(long length);

    Charset charset();

    OutputStream outputStream(int bufferSize) throws IOException;

    void body(String text) throws IOException;

    void body(Body body) throws IOException;

    Body body();

    void reset() throws IOException;

    <T> T unwrap(Class<T> type);
}
