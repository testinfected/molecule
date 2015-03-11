package com.vtence.molecule.middlewares;

import com.vtence.molecule.Request;
import com.vtence.molecule.Response;
import com.vtence.molecule.http.Cookie;
import com.vtence.molecule.lib.CookieJar;

public class Cookies extends AbstractMiddleware {
    public void handle(Request request, Response response) throws Exception {
        CookieJar cookies = new CookieJar();

        cookies.add(new Cookie("foo", "bar"));
        cookies.add(new Cookie("baz", "qux"));
        cookies.add(new Cookie("profile", "wine lover"));
        cookies.add(new Cookie("location", "quebec"));

        cookies.bind(request);
        forward(request, response);
    }
}
