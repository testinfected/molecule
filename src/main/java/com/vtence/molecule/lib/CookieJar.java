package com.vtence.molecule.lib;

import com.vtence.molecule.Request;
import com.vtence.molecule.http.Cookie;

import java.util.HashMap;
import java.util.Map;

public class CookieJar {
    private final Map<String, Cookie> cookies = new HashMap<String, Cookie>();

    public static CookieJar get(Request request) {
        return request.attribute(CookieJar.class);
    }

    public void bind(Request request) {
        request.attribute(CookieJar.class, this);
    }

    public Cookie get(String name) {
        return cookies.get(name);
    }

    public void add(Cookie cookie) {
        cookies.put(cookie.name(), cookie);
    }
}