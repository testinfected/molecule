package com.vtence.molecule.middlewares;

import com.vtence.molecule.Request;
import com.vtence.molecule.Response;
import com.vtence.molecule.http.Cookie;
import com.vtence.molecule.http.CookieDecoder;
import com.vtence.molecule.lib.CookieJar;

import java.util.Collections;
import java.util.List;

import static com.vtence.molecule.http.HeaderNames.COOKIE;
import static com.vtence.molecule.http.HeaderNames.SET_COOKIE;

public class Cookies extends AbstractMiddleware {

    private final CookieDecoder cookieDecoder = new CookieDecoder();

    public void handle(Request request, Response response) throws Exception {
        CookieJar cookies = new CookieJar(clientCookiesFrom(request));
        cookies.bind(request);
        try {
            forward(request, response);
        } finally {
            setCookies(response, cookies);
        }
    }

    private void setCookies(Response response, CookieJar cookies) {
        for (Cookie cookie : cookies.fresh()) {
            response.addHeader(SET_COOKIE, cookie.toString());
        }
        for (Cookie cookie : cookies.discarded()) {
            response.addHeader(SET_COOKIE, cookie.maxAge(0).toString());
        }
    }

    private List<Cookie> clientCookiesFrom(Request request) {
        String cookieHeader = request.header(COOKIE);
        return cookieHeader != null ? cookieDecoder.decode(cookieHeader) : Collections.<Cookie>emptyList();
    }
}