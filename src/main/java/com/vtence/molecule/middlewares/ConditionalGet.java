package com.vtence.molecule.middlewares;

import com.vtence.molecule.Request;
import com.vtence.molecule.Response;
import com.vtence.molecule.http.HttpMethod;
import com.vtence.molecule.lib.AbstractMiddleware;

import static com.vtence.molecule.http.HeaderNames.*;
import static com.vtence.molecule.http.HttpStatus.NOT_MODIFIED;
import static com.vtence.molecule.http.HttpStatus.OK;
import static com.vtence.molecule.lib.BinaryBody.empty;

public class ConditionalGet extends AbstractMiddleware {

    public void handle(Request request, Response response) throws Exception {
        forward(request, response);

        if (get(request) && ok(response) && etagMatches(request, response)) {
            response.body(empty());
            response.remove(CONTENT_TYPE);
            response.remove(CONTENT_LENGTH);
            response.status(NOT_MODIFIED);
        }
    }

    private boolean get(Request request) {
        return request.method() == HttpMethod.GET;
    }

    private boolean ok(Response response) {
        return response.statusCode() == OK.code;
    }

    private boolean etagMatches(Request request, Response response) {
        String etag = response.get(ETAG);
        String noneMatch = request.header(IF_NONE_MATCH);
        return etag != null && etag.equals(noneMatch);
    }
}