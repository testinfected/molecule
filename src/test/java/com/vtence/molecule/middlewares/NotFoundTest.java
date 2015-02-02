package com.vtence.molecule.middlewares;

import com.vtence.molecule.Request;
import com.vtence.molecule.Response;
import org.junit.Before;
import org.junit.Test;

import static com.vtence.molecule.http.HttpStatus.NOT_FOUND;
import static com.vtence.molecule.support.ResponseAssertions.assertThat;

public class NotFoundTest {

    NotFound notFound = new NotFound();

    Request request = new Request();
    Response response = new Response();

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
        assertThat(response).hasBodyText(content);
    }

    @Test public void
    setsContentTypeToPlainText() {
       assertThat(response).hasContentType("text/plain");
    }
}