package com.vtence.molecule.http;

import com.vtence.molecule.Request;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

public class AuthorizationTest {

    @Test
    public void needsAuthorizationHeaderToBeProvided() throws Exception {
        Authorization noAuth = Authorization.of(Request.get("/"));
        assertThat("not provided", noAuth, nullValue());

        Authorization auth = Authorization.of(Request.get("/").header("Authorization", "..."));
        assertThat("provided", auth, notNullValue());
    }

    @Test
    public void
    parsesSchemeFromHeader() throws Exception {
        Authorization auth = Authorization.of(Request.get("/").header("Authorization", "Basic"));

        assertThat("scheme", auth.scheme(), equalTo("Basic"));
    }

    @Test
    public void
    considersMissingSchemeAsEmpty() throws Exception {
        Authorization auth = Authorization.of(Request.get("/").header("Authorization", ""));

        assertThat("missing scheme", auth.scheme(), is(emptyString()));
    }

    @Test
    public void
    parsesAuthParamsFromHeader() throws Exception {
        Authorization auth = Authorization.of(Request.get("/").header("Authorization", "Basic <credentials>"));

        assertThat("params", auth.params(), equalTo("<credentials>"));
    }

    @Test
    public void
    considersMissingParamsAsEmpty() throws Exception {
        Authorization auth = Authorization.of(Request.get("/").header("Authorization", "Basic"));

        assertThat("missing params", auth.params(), is(emptyString()));
    }
}
