package com.vtence.molecule.middlewares;

import com.vtence.molecule.support.MockRequest;
import com.vtence.molecule.support.MockResponse;
import org.junit.Before;
import org.junit.Test;

import static com.vtence.molecule.http.HttpStatus.NOT_FOUND;
import static com.vtence.molecule.support.ResponseAssertions.assertThat;

public class NotFoundTest {

    NotFound notFound = new NotFound();

    MockRequest request = new MockRequest();
    MockResponse response = new MockResponse();

    String content = "Not found: /resource";

    @Before public void
    handleRequest() throws Exception {
        notFound.handle(request.path("/resource"), response);
    }

    @Test public void
    setsStatusCodeToNotFound() {
        assertThat(response).hasStatus(NOT_FOUND);
    }

    @Test public void
    rendersPageNotFound() {
        response.assertBody(content);
    }

    @Test public void
    setsContentTypeToPlainText() {
       assertThat(response).hasContentType("text/plain");
    }
}