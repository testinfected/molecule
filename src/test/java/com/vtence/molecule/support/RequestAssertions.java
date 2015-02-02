package com.vtence.molecule.support;

import com.vtence.molecule.Request;
import org.hamcrest.Matcher;
import org.junit.Assert;

import static org.hamcrest.CoreMatchers.equalTo;

public class RequestAssertions {

    private final Request request;

    protected RequestAssertions(Request request) {
        this.request = request;
    }

    public static RequestAssertions assertThat(Request request) {
        return new RequestAssertions(request);
    }

    public RequestAssertions hasAttribute(Object key, Object value) {
        return hasAttribute(key, equalTo(value));
    }

    public RequestAssertions hasAttribute(Object key, Matcher<Object> matching) {
        Assert.assertThat("request attribute '" + key.toString() + "'", request.attribute(key), matching);
        return this;
    }
}