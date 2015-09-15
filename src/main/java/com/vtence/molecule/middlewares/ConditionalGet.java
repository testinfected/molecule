package com.vtence.molecule.middlewares;

import com.vtence.molecule.Request;
import com.vtence.molecule.Response;
import com.vtence.molecule.http.HttpDate;
import com.vtence.molecule.http.HttpMethod;

import java.util.function.Consumer;

import static com.vtence.molecule.http.HeaderNames.CONTENT_LENGTH;
import static com.vtence.molecule.http.HeaderNames.CONTENT_TYPE;
import static com.vtence.molecule.http.HeaderNames.ETAG;
import static com.vtence.molecule.http.HeaderNames.IF_MODIFIED_SINCE;
import static com.vtence.molecule.http.HeaderNames.IF_NONE_MATCH;
import static com.vtence.molecule.http.HeaderNames.LAST_MODIFIED;
import static com.vtence.molecule.http.HttpMethod.GET;
import static com.vtence.molecule.http.HttpMethod.HEAD;
import static com.vtence.molecule.http.HttpStatus.NOT_MODIFIED;
import static com.vtence.molecule.http.HttpStatus.OK;
import static com.vtence.molecule.lib.BinaryBody.empty;

public class ConditionalGet extends AbstractMiddleware {

    public void handle(Request request, Response response) throws Exception {
        forward(request, response).whenSuccessful(conditionalGet(request));
    }

    private Consumer<Response> conditionalGet(Request request) {
        return response -> {
            if (supported(request.method()) && ok(response) && stillFresh(request, response)) {
                response.body(empty());
                response.removeHeader(CONTENT_TYPE);
                response.removeHeader(CONTENT_LENGTH);
                response.status(NOT_MODIFIED);
            }
        };
    }

    private boolean supported(HttpMethod method) {
        return method == GET || method == HEAD;
    }

    private boolean ok(Response response) {
        return response.statusCode() == OK.code;
    }

    private boolean stillFresh(Request request, Response response) {
        String etag = request.header(IF_NONE_MATCH);
        String lastTimeSeen = request.header(IF_MODIFIED_SINCE);

        if (etag == null && lastTimeSeen == null) return false;
        if (lastTimeSeen != null && modifiedSince(lastTimeSeen, response)) return false;
        if (etag != null && !current(etag, response)) return false;

        return true;
    }

    private boolean current(String noneMatch, Response response) {
        String etag = response.header(ETAG);
        return (etag != null) && etag.equals(noneMatch);
    }

    private boolean modifiedSince(String modifiedSince, Response response) {
        String lastModified = response.header(LAST_MODIFIED);
        return lastModified == null || !HttpDate.parse(lastModified).equals(HttpDate.parse(modifiedSince));
    }
}