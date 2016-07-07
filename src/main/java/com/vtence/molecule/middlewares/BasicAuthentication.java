package com.vtence.molecule.middlewares;

import com.vtence.molecule.Request;
import com.vtence.molecule.Response;

import static com.vtence.molecule.http.HeaderNames.WWW_AUTHENTICATE;
import static com.vtence.molecule.http.HttpStatus.UNAUTHORIZED;
import static com.vtence.molecule.http.MimeTypes.TEXT;

public class BasicAuthentication extends AbstractMiddleware {
    private final String realm;

    public BasicAuthentication(String realm) {
        this.realm = realm;
    }

    public void handle(Request request, Response response) throws Exception {
        response.status(UNAUTHORIZED)
                .addHeader(WWW_AUTHENTICATE, challenge())
                .contentType(TEXT)
                .done();
    }

    private String challenge() {
        return String.format("Basic realm=\"%s\"", realm);
    }
}
