package com.vtence.molecule.middlewares;

import com.vtence.molecule.Application;
import com.vtence.molecule.Middleware;
import com.vtence.molecule.Response;

import static com.vtence.molecule.http.HeaderNames.CONTENT_LENGTH;
import static com.vtence.molecule.http.HeaderNames.TRANSFER_ENCODING;

public class ContentLengthHeader implements Middleware {

    public Application then(Application next) {
        return Application.of(request -> next.handle(request)
                                             .whenSuccessful(this::addContentLengthHeader));
    }

    private void addContentLengthHeader(Response response) {
        if (requiresContentLengthHeader(response)) response.contentLength(response.size());
    }

    public boolean requiresContentLengthHeader(Response response) {
        return !hasContentLengthHeader(response) && isFixedLengthSize(response) && !isChunked(response);
    }

    private boolean isFixedLengthSize(Response response) {
        return response.size() > 0;
    }

    private boolean hasContentLengthHeader(Response response) {
        return response.hasHeader(CONTENT_LENGTH);
    }

    private boolean isChunked(Response response) {
        return response.hasHeader(TRANSFER_ENCODING)
                && response.header(TRANSFER_ENCODING).equalsIgnoreCase("chunked");
    }
}