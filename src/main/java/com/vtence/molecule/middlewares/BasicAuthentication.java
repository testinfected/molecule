package com.vtence.molecule.middlewares;

import com.vtence.molecule.Request;
import com.vtence.molecule.Response;
import com.vtence.molecule.http.Authorization;

import static com.vtence.molecule.http.HeaderNames.WWW_AUTHENTICATE;
import static com.vtence.molecule.http.HttpStatus.BAD_REQUEST;
import static com.vtence.molecule.http.HttpStatus.UNAUTHORIZED;
import static com.vtence.molecule.http.MimeTypes.TEXT;

public class BasicAuthentication extends AbstractMiddleware {

    private static final String BASIC_AUTHENTICATION = "Basic";

    private final String realm;

    public BasicAuthentication(String realm) {
        this.realm = realm;
    }

    public void handle(Request request, Response response) throws Exception {
        Authorization auth = Authorization.of(request);

        if (auth == null) {
            response.status(UNAUTHORIZED)
                    .addHeader(WWW_AUTHENTICATE, challenge())
                    .contentType(TEXT)
                    .done();
            return;
        }

        if (!auth.hasScheme(BASIC_AUTHENTICATION)) {
            response.status(BAD_REQUEST).done();
        }

        response.done();
    }

    private String challenge() {
        return String.format("Basic realm=\"%s\"", realm);
    }
}
