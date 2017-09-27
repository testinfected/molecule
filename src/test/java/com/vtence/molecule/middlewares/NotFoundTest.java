package com.vtence.molecule.middlewares;

import com.vtence.molecule.Request;
import com.vtence.molecule.Response;
import org.junit.Test;

import static com.vtence.molecule.http.HttpStatus.NOT_FOUND;
import static com.vtence.molecule.testing.ResponseAssert.assertThat;

public class NotFoundTest {

    NotFound notFound = new NotFound();

    @Test public void
    setsStatusCodeToNotFound() throws Exception {
        assertThat(fetchNotFound()).hasStatus(NOT_FOUND);
    }

    @Test public void
    rendersPageNotFound() throws Exception {
        assertThat(fetchNotFound()).hasBodyText("Not found: /resource");
    }

    @Test public void
    setsContentTypeToPlainText() throws Exception {
        assertThat(fetchNotFound()).hasContentType("text/plain");
    }

    @Test public void
    completesResponse() throws Exception {
        assertThat(fetchNotFound()).isDone();
    }

    private Response fetchNotFound() throws Exception {
        return notFound.handle(Request.get("/resource"));
    }
}