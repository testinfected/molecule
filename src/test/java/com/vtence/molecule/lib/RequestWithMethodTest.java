package com.vtence.molecule.lib;

import com.vtence.molecule.Request;
import com.vtence.molecule.support.MockRequest;
import org.junit.Test;

import static com.vtence.molecule.http.HttpMethod.GET;
import static com.vtence.molecule.http.HttpMethod.POST;
import static com.vtence.molecule.lib.Matchers.withMethod;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class RequestWithMethodTest {

    @SuppressWarnings("unchecked")
    @Test public void
    matchesWhenRequestMethodsAreEqual() {
        Request request = MockRequest.GET("/");

        assertThat("same case match", withMethod("GET").matches(request), is(true));
        assertThat("different case match", withMethod("get").matches(request), is(true));
        assertThat("method match", withMethod(GET).matches(request), is(true));
        assertThat("method mismatch", withMethod(POST).matches(request), is(false));
    }
}