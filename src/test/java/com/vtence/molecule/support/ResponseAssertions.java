package com.vtence.molecule.support;

import com.vtence.molecule.Response;
import com.vtence.molecule.http.Cookie;
import com.vtence.molecule.http.HeaderNames;
import com.vtence.molecule.http.HttpStatus;
import org.hamcrest.Matcher;
import org.junit.Assert;

import static com.vtence.molecule.http.HeaderNames.CONTENT_TYPE;
import static com.vtence.molecule.http.HeaderNames.LOCATION;
import static org.hamcrest.CoreMatchers.*;

public class ResponseAssertions {

    private final Response response;

    protected ResponseAssertions(Response response) {
        this.response = response;
    }

    public static ResponseAssertions assertThat(Response response) {
        return new ResponseAssertions(response);
    }

    public ResponseAssertions hasStatusCode(int code) {
        hasStatusCode(is(code));
        return this;
    }

    public ResponseAssertions hasStatusCode(Matcher<? super Integer> matching) {
        Assert.assertThat("response status code", response.statusCode(), matching);
        return this;
    }

    public ResponseAssertions hasStatusText(String text) {
        hasStatusText(is(text));
        return this;
    }

    public ResponseAssertions hasStatusText(Matcher<? super String> matching) {
        Assert.assertThat("response status text", response.statusText(), matching);
        return this;
    }

    public ResponseAssertions hasStatus(HttpStatus expected) {
        hasStatusCode(expected.code);
        hasStatusText(expected.text);
        return this;
    }

    public ResponseAssertions isRedirectedTo(String location) {
        isRedirectedTo(equalTo(location));
        return this;
    }

    public ResponseAssertions isRedirectedTo(Matcher<? super String> matching) {
        hasHeader(HeaderNames.LOCATION, matching);
        return this;
    }

    public ResponseAssertions hasHeader(String named) {
        hasHeader(named, any(String.class));
        return this;
    }

    public ResponseAssertions hasHeader(String name, String value) {
        hasHeader(name, equalTo(value));
        return this;
    }

    public ResponseAssertions hasHeader(String name, Matcher<? super String> matchingValue) {
        Assert.assertThat("response " + name + " header", response.header(name), matchingValue);
        return this;
    }

    public ResponseAssertions hasNoHeader(String named) {
        hasHeader(named, nullValue());
        return this;
    }

    public ResponseAssertions hasContentType(String contentType) {
        hasContentType(equalTo(contentType));
        return this;
    }

    public ResponseAssertions hasContentType(Matcher<? super String> matching) {
        hasHeader(CONTENT_TYPE, matching);
        return this;
    }

    public CookieAssertions hasCookie(String named) {
        Cookie cookie = response.cookie(named);
        Assert.assertTrue("response is missing cookie " + named, cookie != null);
        return new CookieAssertions(cookie);
    }

    public ResponseAssertions hasNoCookie(String named) {
        Assert.assertFalse("response has unexpected cookie " + named, response.hasCookie(named));
        return this;
    }
}
