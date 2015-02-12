package com.vtence.molecule.testing;

import com.vtence.molecule.Request;
import org.hamcrest.Matcher;
import org.junit.Assert;

import static org.hamcrest.CoreMatchers.equalTo;

public class RequestAssert {

    private final Request request;

    protected RequestAssert(Request request) {
        this.request = request;
    }

    public static RequestAssert assertThat(Request request) {
        return new RequestAssert(request);
    }

    public RequestAssert hasAttribute(Object key, Object value) {
        return hasAttribute(key, equalTo(value));
    }

    public RequestAssert hasAttribute(Object key, Matcher<Object> matching) {
        Assert.assertThat("request attribute '" + key.toString() + "'", request.attribute(key), matching);
        return this;
    }
}