package com.vtence.molecule;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface Request {

    String body() throws IOException;

    long contentLength();

    String contentType();

    String header(String name);

    List<String> headers(String name);

    List<String> headerNames();

    HttpMethod method();

    String uri();

    String pathInfo();

    String ip();

    String protocol();

    String parameter(String name);

    String[] parameters(String name);

    Cookie cookie(String name);

    String cookieValue(String name);

    List<Cookie> cookies();

    <T> T attribute(Object key);

    Map<Object, Object> attributes();

    void attribute(Object key, Object value);

    void removeAttribute(Object key);

    <T> T unwrap(Class<T> type);
}