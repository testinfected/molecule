package com.vtence.molecule;

import java.io.IOException;
import java.util.Map;

public interface Request {

    String protocol();

    String body() throws IOException;

    HttpMethod method();

    String uri();

    String pathInfo();

    String parameter(String name);

    String ip();

    Object attribute(Object key);

    void attribute(Object key, Object value);

    void removeAttribute(Object key);

    Map<Object, Object> attributes();

    String cookie(String name);

    Session session();

    Session session(boolean create);

    <T> T unwrap(Class<T> type);
}