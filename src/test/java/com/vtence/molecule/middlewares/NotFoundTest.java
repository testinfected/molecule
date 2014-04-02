package com.vtence.molecule.middlewares;

import com.vtence.molecule.HttpStatus;
import com.vtence.molecule.support.MockRequest;
import com.vtence.molecule.support.MockResponse;
import org.junit.Before;
import org.junit.Test;

import static com.vtence.molecule.support.MockRequest.aRequest;
import static com.vtence.molecule.support.MockResponse.aResponse;

public class NotFoundTest {

    NotFound notFound = new NotFound();

    MockRequest request = aRequest().withPath("/resource");
    MockResponse response = aResponse();

    String content = "Not found: /resource";

    @Before public void
    handleRequest() throws Exception {
        notFound.handle(request, response);
    }

    @Test public void
    setsStatusCodeToNotFound() {
        response.assertStatus(HttpStatus.NOT_FOUND);
    }

    @Test public void
    rendersPageNotFound() {
        response.assertBody(content);
    }

    @Test public void
    setsContentTypeToPlainText() {
        response.assertHeader("Content-Type", "text/plain");
    }
}