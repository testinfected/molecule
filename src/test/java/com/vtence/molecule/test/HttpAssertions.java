package com.vtence.molecule.test;

import org.hamcrest.Matcher;
import org.junit.Assert;

import java.util.List;

import static com.vtence.molecule.http.HeaderNames.TRANSFER_ENCODING;
import static com.vtence.molecule.support.CharsetDetector.detectCharsetOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;

public class HttpAssertions {
    private final HttpResponse response;

    public HttpAssertions(HttpResponse response) {
        this.response = response;
    }

    public static HttpAssertions assertThat(HttpResponse response) {
        return new HttpAssertions(response);
    }

    public HttpAssertions isOK() {
        return hasStatusCode(200);
    }

    public HttpAssertions hasStatusCode(int code) {
        return hasStatusCode(is(code));
    }

    private HttpAssertions hasStatusCode(Matcher<? super Integer> matching) {
        Assert.assertThat("response status code", response.statusCode(), matching);
        return this;
    }

    public HttpAssertions hasStatusMessage(String message) {
        return hasStatusMessage(is(message));
    }

    public HttpAssertions hasStatusMessage(Matcher<? super String> matching) {
        Assert.assertThat("response status message", response.statusMessage(), matching);
        return this;
    }

    public HttpAssertions hasHeader(String name, String value) {
        return hasHeader(name, equalTo(value));
    }

    public HttpAssertions hasHeader(String name, Matcher<? super String> matching) {
        Assert.assertThat("response '" + name + "' header", response.header(name), matching);
        return this;
    }

    public HttpAssertions hasHeaders(String name, Matcher<? super List<String>> matching) {
        Assert.assertThat("response '" + name + "' headers", response.headers(name), matching);
        return this;
    }

    public HttpAssertions isChunked() {
        return hasHeader(TRANSFER_ENCODING, "chunked");
    }

    public HttpAssertions isNotChunked() {
        return hasHeader(TRANSFER_ENCODING, not("chunked"));
    }

    public HttpAssertions hasBodyText(String text) {
        return hasBodyText(equalTo(text));
    }

    public HttpAssertions hasBodyText(Matcher<? super String> matching) {
        Assert.assertThat("response body text", response.bodyText(), matching);
        return this;
    }

    public HttpAssertions hasContentEncodedAs(String charset) {
        return hasContentEncodedAs(is(charset));
    }

    public HttpAssertions hasContentEncodedAs(Matcher<? super String> matching) {
        Assert.assertThat("response content encoding", detectCharsetOf(response.body()), matching);
        return this;
    }

}