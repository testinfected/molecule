package com.vtence.molecule;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface Request {

    String body() throws IOException;

    long contentLength();

    String contentType();

    String header(String name);

    List<String> headers(String name);

    Set<String> headerNames();

    HttpMethod method();

    String uri();

    String pathInfo();

    String ip();

    String protocol();

    String parameter(String name);

    String[] parameters(String name);

    String cookie(String name);

    Map<String, String> cookies();

    Object attribute(Object key);

    Map<Object, Object> attributes();

    void attribute(Object key, Object value);

    void removeAttribute(Object key);

    Session session();

    Session session(boolean create);

    <T> T unwrap(Class<T> type);
}