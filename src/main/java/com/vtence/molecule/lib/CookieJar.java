package com.vtence.molecule.lib;

import com.vtence.molecule.Request;
import com.vtence.molecule.http.Cookie;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CookieJar {
    private final Map<String, Cookie> cookies = new LinkedHashMap<String, Cookie>();
    private final Map<String, Cookie> fresh = new LinkedHashMap<String, Cookie>();
    private final Map<String, Cookie> discarded = new LinkedHashMap<String, Cookie>();

    public CookieJar(Iterable<Cookie> cookies) {
        for (Cookie cookie : cookies) {
            store(cookie);
        }
    }

    public static CookieJar get(Request request) {
        return request.attribute(CookieJar.class);
    }

    public void bind(Request request) {
        request.attribute(CookieJar.class, this);
    }

    public void unbind(Request request) {
        request.removeAttribute(CookieJar.class);
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

    public CookieJar add(String name, String value) {
        return add(new Cookie(name, value));
    }

    public CookieJar add(Cookie cookie) {
        store(cookie);
        fresh.put(cookie.name(), cookie);
        return this;
    }

    public CookieJar discard(String name) {
        return discard(new Cookie(name, ""));
    }

    private CookieJar discard(Cookie cookie) {
        cookies.remove(cookie.name());
        discarded.put(cookie.name(), cookie);
        return this;
    }

    public List<Cookie> list() {
        return new ArrayList<Cookie>(cookies.values());
    }

    public List<Cookie> fresh() {
        return new ArrayList<Cookie>(fresh.values());
    }

    public List<Cookie> discarded() {
        return new ArrayList<Cookie>(discarded.values());
    }

    private void store(Cookie cookie) {
        cookies.put(cookie.name(), cookie);
    }
}