package com.vtence.molecule.middlewares;

import com.vtence.molecule.Request;
import com.vtence.molecule.Response;
import com.vtence.molecule.http.Cookie;
import com.vtence.molecule.http.CookieDecoder;
import com.vtence.molecule.lib.CookieJar;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import static com.vtence.molecule.http.HeaderNames.COOKIE;
import static com.vtence.molecule.http.HeaderNames.SET_COOKIE;

public class Cookies extends AbstractMiddleware {

    private final CookieDecoder cookieDecoder = new CookieDecoder();

    public void handle(Request request, Response response) throws Exception {
        CookieJar cookieJar = new CookieJar(clientCookiesFrom(request));
        cookieJar.bind(request);
        try {
            forward(request, response).whenSuccessful(commitCookies(cookieJar))
                                      .whenComplete((result, error) -> cookieJar.unbind(request));
        } catch (Throwable error) {
            cookieJar.unbind(request);
            throw error;
        }
    }

    private Consumer<Response> commitCookies(CookieJar cookies) {
        return response -> {
            for (Cookie cookie : cookies.fresh()) {
                response.addCookie(cookie);
            }

            for (Cookie cookie : cookies.discarded()) {
                response.addCookie(cookie.maxAge(0));
            }
        };
    }

    private List<Cookie> clientCookiesFrom(Request request) {
        String cookieHeader = request.header(COOKIE);
        return cookieHeader != null ? cookieDecoder.decode(cookieHeader) : Collections.<Cookie>emptyList();
    }
}