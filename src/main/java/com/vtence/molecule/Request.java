package com.vtence.molecule;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface Request {

    String body() throws IOException;

    long contentLength();

    String contentType();

    List<String> headers();

    List<String> headers(String name);

    String header(String name);

    HttpMethod method();

    String parameter(String name);

    String uri();

    String pathInfo();

    String ip();

    String protocol();

    Map<String, String> cookies();

    String cookie(String name);

    Object attribute(Object key);

    Map<Object, Object> attributes();

    void attribute(Object key, Object value);

    void removeAttribute(Object key);

    Session session();

    Session session(boolean create);

    <T> T unwrap(Class<T> type);
}