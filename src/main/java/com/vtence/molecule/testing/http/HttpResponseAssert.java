package com.vtence.molecule.testing.http;

import org.hamcrest.Matcher;
import org.hamcrest.MatcherAssert;

import java.net.HttpCookie;
import java.util.List;

import static com.vtence.molecule.testing.CharsetDetector.detectCharsetOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.equalToIgnoringCase;

public class HttpResponseAssert {
    private final HttpResponse response;

    public HttpResponseAssert(HttpResponse response) {
        this.response = response;
    }

    public static HttpResponseAssert assertThat(HttpResponse response) {
        return new HttpResponseAssert(response);
    }

    public HttpResponseAssert isOK() {
        return hasStatusCode(200);
    }

    public HttpResponseAssert hasStatusCode(int code) {
        return hasStatusCode(is(code));
    }

    private HttpResponseAssert hasStatusCode(Matcher<? super Integer> matching) {
        MatcherAssert.assertThat("response status code", response.statusCode(), matching);
        return this;
    }

    public HttpResponseAssert hasStatusMessage(String message) {
        return hasStatusMessage(is(message));
    }

    public HttpResponseAssert hasStatusMessage(Matcher<? super String> matching) {
        MatcherAssert.assertThat("response status message", response.statusMessage(), matching);
        return this;
    }

    public HttpResponseAssert hasHeader(String named) {
        return hasHeader(named, notNullValue());
    }

    public HttpResponseAssert hasNoHeader(String named) {
        return hasHeader(named, nullValue());
    }

    public HttpResponseAssert hasHeader(String name, String value) {
        return hasHeader(name, equalTo(value));
    }

    public HttpResponseAssert hasHeader(String name, Matcher<? super String> matching) {
        MatcherAssert.assertThat("response '" + name + "' header", response.header(name), matching);
        return this;
    }

    public HttpResponseAssert hasHeaders(String name, Matcher<? super List<String>> matching) {
        MatcherAssert.assertThat("response '" + name + "' headers", response.headers(name), matching);
        return this;
    }

    public HttpResponseAssert isChunked() {
        return hasHeader("Transfer-Encoding", "chunked");
    }

    public HttpResponseAssert isNotChunked() {
        return hasHeader("Transfer-Encoding", not("chunked"));
    }

    public HttpResponseAssert hasBodyText(String text) {
        return hasBodyText(equalTo(text));
    }

    public HttpResponseAssert hasBodyText(Matcher<? super String> matching) {
        MatcherAssert.assertThat("response body text", response.bodyText(), matching);
        return this;
    }

    public HttpResponseAssert isEmpty() {
        return hasBodySize(0);
    }

    public HttpResponseAssert hasBodySize(int size) {
        return hasBodySize(is(size));
    }

    public HttpResponseAssert hasBodySize(Matcher<? super Integer> matching) {
        MatcherAssert.assertThat("response body size", response.body().length, matching);
        return this;
    }

    public HttpResponseAssert hasContentEncodedAs(String charset) {
        return hasContentEncodedAs(equalToIgnoringCase(charset));
    }

    public HttpResponseAssert hasContentEncodedAs(Matcher<? super String> matching) {
        MatcherAssert.assertThat("response content encoding", detectCharsetOf(response.body()), matching);
        return this;
    }

    public HttpCookieAssert hasCookie(String named) {
        HttpCookie cookie = response.cookie(named);
        MatcherAssert.assertThat("response contains no cookie named '" + named + "'", cookie != null);
        return HttpCookieAssert.assertThat(cookie);
    }

    public HttpResponseAssert hasNoCookie(String named) {
        HttpCookie cookie = response.cookie(named);
        MatcherAssert.assertThat("response contains unexpected cookie '" + named + "'", cookie == null);
        return this;
    }

    public HttpResponseAssert hasContentType(String contentType) {
        return hasContentType(equalTo(contentType));
    }

    public HttpResponseAssert hasContentType(Matcher<? super String> matching) {
        return hasHeader("Content-Type", matching);
    }
}