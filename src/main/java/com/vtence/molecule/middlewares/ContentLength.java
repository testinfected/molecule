package com.vtence.molecule.middlewares;

import com.vtence.molecule.Request;
import com.vtence.molecule.Response;

import static com.vtence.molecule.HttpHeaders.CONTENT_LENGTH;
import static com.vtence.molecule.HttpHeaders.TRANSFER_ENCODING;
import static java.lang.String.valueOf;

public class ContentLength extends AbstractMiddleware {

    public void handle(Request request, Response response) throws Exception {
        forward(request, response);
        if (!hasContentLengthHeader(response)
                && isFixedLengthSize(response)
                && !isChunked(response)) {
            response.header(CONTENT_LENGTH, valueOf(response.size()));
        }
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
