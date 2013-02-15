package com.vtence.molecule;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.charset.Charset;

public interface Response {

    int statusCode();

    void statusCode(int code);

    void status(HttpStatus status);

    String header(String name);

    void header(String name, String value);

    void headerDate(String name, long date);

    void removeHeader(String name);

    void contentType(String contentType);

    String contentType();

    int contentLength();

    void contentLength(int length);

    Charset charset();

    void redirectTo(String location);

    OutputStream outputStream() throws IOException;

    OutputStream outputStream(int bufferSize) throws IOException;

    Writer writer() throws IOException;

    void body(String body) throws IOException;

    void reset() throws IOException;

    <T> T unwrap(Class<T> type);
}
