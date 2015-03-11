package com.vtence.molecule.lib;

import com.vtence.molecule.Request;
import com.vtence.molecule.http.Cookie;

public class CookieJar {
    public static CookieJar get(Request request) {
        return new CookieJar();
    }

    public Cookie get(String name) {
        if (name.equals("profile")) return new Cookie(name, "wine lover");
        if (name.equals("location")) return new Cookie(name, "quebec");
        return null;
    }
}
