package com.vtence.molecule.testing.http;

import org.hamcrest.Matcher;
import org.hamcrest.MatcherAssert;

import java.net.HttpCookie;
import java.net.http.HttpResponse;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.*;

public class HttpResponseAssert<T> {
    private final HttpResponse<T> response;

    public HttpResponseAssert(HttpResponse<T> response) {
        this.response = response;
    }

    public static <T> HttpResponseAssert<T> assertThat(HttpResponse<T> response) {
        return new HttpResponseAssert<>(response);
    }

    public HttpResponseAssert<T> isOK() {
        return hasStatusCode(200);
    }

    public HttpResponseAssert<T> hasStatusCode(int code) {
        return hasStatusCode(is(code));
    }

    private HttpResponseAssert<T> hasStatusCode(Matcher<? super Integer> matching) {
        MatcherAssert.assertThat("response status code", response.statusCode(), matching);
        return this;
    }

    public HttpResponseAssert<T> isChunked() {
        return hasHeader("Transfer-Encoding", "chunked");
    }

    public HttpResponseAssert<T> isNotChunked() {
        return hasHeader("Transfer-Encoding", not("chunked"));
    }

    public HttpResponseAssert<T> hasBody(T body) {
        return hasBody(equalTo(body));
    }

    public HttpResponseAssert<T> hasBody(Matcher<? super T> matching) {
        MatcherAssert.assertThat("response body", response.body(), matching);
        return this;
    }

    public HttpResponseAssert<T> hasContentType(String contentType) {
        return hasContentType(equalTo(contentType));
    }

    public HttpResponseAssert<T> hasContentType(Matcher<? super String> matching) {
        return hasHeader("Content-Type", matching);
    }

    public HttpResponseAssert<T> hasHeader(String named) {
        return hasHeader(named, notNullValue());
    }

    public HttpResponseAssert<T> hasNoHeader(String named) {
        return hasHeader(named, nullValue());
    }

    public HttpResponseAssert<T> hasHeader(String name, String value) {
        return hasHeader(name, equalTo(value));
    }

    public HttpResponseAssert<T> hasHeader(String name, Matcher<? super String> matching) {
        MatcherAssert.assertThat("response '" + name + "' header", response.headers().firstValue(name).orElse(null), matching);
        return this;
    }

    public HttpResponseAssert<T> hasHeaders(String name, Matcher<? super Collection<String>> matching) {
        MatcherAssert.assertThat("response '" + name + "' headers", response.headers().allValues(name), matching);
        return this;
    }

    public HttpCookieAssert hasCookie(String named) {
        HttpCookie cookie = cookie(named);
        MatcherAssert.assertThat("response contains no cookie named '" + named + "'", cookie != null);
        return HttpCookieAssert.assertThat(cookie);
    }

    public HttpResponseAssert<T> hasNoCookie(String named) {
        HttpCookie cookie = cookie(named);
        MatcherAssert.assertThat("response contains unexpected cookie '" + named + "'", cookie == null);
        return this;
    }

    private Map<String, HttpCookie> cookies() {
        Map<String, HttpCookie> cookies = new HashMap<>();
        for (String header : response.headers().allValues("Set-Cookie")) {
            for (HttpCookie cookie : HttpCookie.parse(header)) {
                cookies.put(cookie.getName(), cookie);
            }
        }
        return cookies;
    }

    private HttpCookie cookie(String name) {
        return cookies().get(name);
    }

    public HttpResponseAssert<T> has(Matcher<? super HttpResponse<T>> feature) {
        MatcherAssert.assertThat(response, feature);
        return this;
    }
}