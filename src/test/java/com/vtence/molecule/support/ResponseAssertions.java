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

    public void hasStatusCode(int code) {
        hasStatusCode(is(code));
    }

    public void hasStatusCode(Matcher<? super Integer> matching) {
        Assert.assertThat("status code", response.statusCode(), matching);
    }

    public void hasStatusText(String text) {
        hasStatusText(is(text));
    }

    public void hasStatusText(Matcher<? super String> matching) {
        Assert.assertThat("status text", response.statusText(), matching);
    }

    public void hasStatus(HttpStatus expected) {
        hasStatusCode(expected.code);
        hasStatusText(expected.text);
    }

    public void isRedirectedTo(String location) {
        isRedirectedTo(equalTo(location));
    }

    public void isRedirectedTo(Matcher<? super String> matching) {
        Assert.assertThat("redirection", response.header(LOCATION), matching);
    }

    public void hasHeader(String name) {
        hasHeader(name, any(String.class));
    }

    public void hasHeader(String name, String value) {
        hasHeader(name, equalTo(value));
    }

    public void hasHeader(String name, Matcher<? super String> matchingValue) {
        Assert.assertThat(name, response.header(name), matchingValue);
    }

    public void hasNoHeader(String name) {
        hasHeader(name, nullValue());
    }

    public void hasContentType(String contentType) {
        hasContentType(equalTo(contentType));
    }

    public void hasContentType(Matcher<? super String> matching) {
        hasHeader(CONTENT_TYPE, matching);
    }
}
