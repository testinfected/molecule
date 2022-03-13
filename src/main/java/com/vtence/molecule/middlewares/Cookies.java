package com.vtence.molecule.middlewares;

import com.vtence.molecule.Application;
import com.vtence.molecule.Middleware;
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

public class Cookies implements Middleware {

    private final CookieDecoder cookieDecoder = new CookieDecoder();

    public Application then(Application next) {
        return request -> {
            var cookieJar = new CookieJar(clientCookiesFrom(request));
            cookieJar.bind(request);
            try {
                return next.handle(request)
                           .whenSuccessful(commitCookies(cookieJar))
                           .whenComplete((result, error) -> cookieJar.unbind(request));
            } catch (Throwable error) {
                cookieJar.unbind(request);
                throw error;
            }
        };
    }

    private Consumer<Response> commitCookies(CookieJar cookies) {
        return response -> {
            for (var cookie : cookies.fresh()) {
                response.addHeader(SET_COOKIE, cookie.toString());
            }

            for (var cookie : cookies.discarded()) {
                response.addHeader(SET_COOKIE, cookie.maxAge(0).toString());
            }
        };
    }

    private List<Cookie> clientCookiesFrom(Request request) {
        String cookieHeader = request.header(COOKIE);
        return cookieHeader != null ? cookieDecoder.decode(cookieHeader) : Collections.<Cookie>emptyList();
    }
}