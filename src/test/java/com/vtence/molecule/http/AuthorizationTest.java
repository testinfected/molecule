package com.vtence.molecule.http;

import com.vtence.molecule.Request;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

public class AuthorizationTest {
    Request request = new Request();

    @Test
    public void needsAuthorizationHeaderToBeProvided() throws Exception {
        Authorization noAuth = Authorization.of(request);
        assertThat("not provided", noAuth, nullValue());

        Authorization auth = Authorization.of(request.header("Authorization", "..."));
        assertThat("provided", auth, notNullValue());
    }

    @Test
    public void
    parsesSchemeFromHeader() throws Exception {
        Authorization auth = Authorization.of(request.header("Authorization", "Basic"));

        assertThat("scheme", auth.scheme(), equalTo("Basic"));
    }

    @Test
    public void
    viewsMissingSchemeAsEmptyScheme() throws Exception {
        Authorization auth = Authorization.of(request.header("Authorization", ""));

        assertThat("scheme", auth.scheme(), is(emptyOrNullString()));
    }
}
