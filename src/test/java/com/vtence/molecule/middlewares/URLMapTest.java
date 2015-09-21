package com.vtence.molecule.middlewares;

import com.vtence.molecule.Request;
import com.vtence.molecule.Response;
import com.vtence.molecule.http.HttpStatus;
import org.junit.Test;

import static com.vtence.molecule.testing.ResponseAssert.assertThat;

public class URLMapTest {

    URLMap urlMap = new URLMap(new NotFound());
    Request request = new Request();
    Response response = new Response();

    @Test
    public void fallsBackToDefaultApplicationForUnmappedPaths() throws Exception {
        request.path("/unmapped");

        urlMap.handle(request, response);
        response.done();

        assertThat(response).hasStatus(HttpStatus.NOT_FOUND)
                            .hasBodyText("Not found: /unmapped");
    }
}