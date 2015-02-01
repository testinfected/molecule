package com.vtence.molecule.support;

import com.vtence.molecule.Response;
import com.vtence.molecule.http.HttpStatus;
import org.hamcrest.Matcher;
import org.junit.Assert;

import static com.vtence.molecule.http.HeaderNames.CONTENT_TYPE;
import static com.vtence.molecule.http.HeaderNames.LOCATION;
import static org.hamcrest.CoreMatchers.*;

public class ResponseAssertions {

    private final Response response;

    public ResponseAssertions(Response response) {
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
        Assert.assertThat("status code", response.statusCode(), matching);
        return this;
    }

    public ResponseAssertions hasStatusText(String text) {
        hasStatusText(is(text));
        return this;
    }

    public ResponseAssertions hasStatusText(Matcher<? super String> matching) {
        Assert.assertThat("status text", response.statusText(), matching);
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
        Assert.assertThat("redirection", response.header(LOCATION), matching);
        return this;
    }

    public ResponseAssertions hasHeader(String name) {
        hasHeader(name, any(String.class));
        return this;
    }

    public ResponseAssertions hasHeader(String name, String value) {
        hasHeader(name, equalTo(value));
        return this;
    }

    public ResponseAssertions hasHeader(String name, Matcher<? super String> matchingValue) {
        Assert.assertThat(name, response.header(name), matchingValue);
        return this;
    }

    public ResponseAssertions hasNoHeader(String name) {
        hasHeader(name, nullValue());
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
}
