package com.vtence.molecule.lib.predicates;

import com.vtence.molecule.Request;
import org.junit.Test;

import static com.vtence.molecule.http.HttpMethod.GET;
import static com.vtence.molecule.http.HttpMethod.POST;
import static com.vtence.molecule.lib.predicates.Predicates.withMethod;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class RequestWithMethodTest {

    @SuppressWarnings("unchecked")
    @Test public void
    matchesWhenRequestMethodsAreEqual() {
        Request request = Request.get("/");

        assertThat("same case match", withMethod("GET").test(request), is(true));
        assertThat("different case match", withMethod("get").test(request), is(true));
        assertThat("method match", withMethod(GET).test(request), is(true));
        assertThat("method mismatch", withMethod(POST).test(request), is(false));
    }
}