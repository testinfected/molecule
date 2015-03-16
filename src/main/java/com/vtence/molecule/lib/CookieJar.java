package com.vtence.molecule.lib;

import com.vtence.molecule.Request;
import com.vtence.molecule.http.Cookie;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CookieJar {
    private final Map<String, Cookie> cookies = new LinkedHashMap<String, Cookie>();

    public CookieJar(Iterable<Cookie> cookies) {
        for (Cookie cookie : cookies) {
            add(cookie);
        }
    }

    public static CookieJar get(Request request) {
        return request.attribute(CookieJar.class);
    }

    public void bind(Request request) {
        request.attribute(CookieJar.class, this);
    }

    public int size() {
        return cookies.size();
    }

    public boolean empty() {
        return cookies.isEmpty();
    }

    public boolean has(String name) {
        return cookies.containsKey(name);
    }

    public Cookie get(String name) {
        return cookies.get(name);
    }

    public void add(Cookie cookie) {
        cookies.put(cookie.name(), cookie);
    }

    public void add(String name, String value) {
        add(new Cookie(name, value));
    }

    public List<Cookie> list() {
        return new ArrayList<Cookie>(cookies.values());
    }
}