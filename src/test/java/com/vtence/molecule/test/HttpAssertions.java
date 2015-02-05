package com.vtence.molecule.test;

import org.hamcrest.Matcher;
import org.junit.Assert;

import static org.hamcrest.CoreMatchers.equalTo;

public class HttpAssertions {
    private final HttpResponse response;

    public HttpAssertions(HttpResponse response) {
        this.response = response;
    }

    public static HttpAssertions assertThat(HttpResponse response) {
        return new HttpAssertions(response);
    }

    public HttpAssertions hasBodyText(String text) {
        return hasBodyText(equalTo(text));
    }

    public HttpAssertions hasBodyText(Matcher<? super String> matching) {
        Assert.assertThat("response body text", response.bodyText(), matching);
        return this;
    }
}
