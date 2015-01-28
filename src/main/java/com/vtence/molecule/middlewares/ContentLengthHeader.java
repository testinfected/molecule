package com.vtence.molecule.middlewares;

import com.vtence.molecule.Request;
import com.vtence.molecule.Response;
import com.vtence.molecule.lib.AbstractMiddleware;

import static com.vtence.molecule.http.HeaderNames.CONTENT_LENGTH;
import static com.vtence.molecule.http.HeaderNames.TRANSFER_ENCODING;

public class ContentLengthHeader extends AbstractMiddleware {

    public void handle(Request request, Response response) throws Exception {
        forward(request, response);
        if (requiresContentLengthHeader(response)) {
            response.contentLength(response.size());
        }
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
