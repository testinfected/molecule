package com.vtence.molecule.support;

import com.vtence.molecule.Response;
import com.vtence.molecule.http.HttpStatus;
import org.hamcrest.Matcher;
import org.junit.Assert;

import static org.hamcrest.core.Is.is;

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
}
