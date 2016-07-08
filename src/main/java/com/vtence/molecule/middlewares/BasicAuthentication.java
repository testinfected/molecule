package com.vtence.molecule.middlewares;

import com.vtence.molecule.Request;
import com.vtence.molecule.Response;
import com.vtence.molecule.http.Authorization;
import com.vtence.molecule.http.BasicCredentials;
import com.vtence.molecule.lib.Authenticator;

import static com.vtence.molecule.http.HeaderNames.WWW_AUTHENTICATE;
import static com.vtence.molecule.http.HttpStatus.BAD_REQUEST;
import static com.vtence.molecule.http.HttpStatus.UNAUTHORIZED;
import static com.vtence.molecule.http.MimeTypes.TEXT;

public class BasicAuthentication extends AbstractMiddleware {

    private static final String BASIC_AUTHENTICATION = "Basic";

    private final String realm;
    private final Authenticator authenticator;

    public BasicAuthentication(String realm, Authenticator authenticator) {
        this.realm = realm;
        this.authenticator = authenticator;
    }

    public void handle(Request request, Response response) throws Exception {
        Authorization auth = Authorization.of(request);

        if (auth == null) {
            unauthorized(response);
            return;
        }

        if (!auth.isForScheme(BASIC_AUTHENTICATION)) {
            response.status(BAD_REQUEST).done();
            return;
        }

        BasicCredentials credentials = BasicCredentials.decode(auth.params());
        authenticator.authenticate(credentials.username(), credentials.password());

        unauthorized(response);
    }

    private void unauthorized(Response response) {
        response.status(UNAUTHORIZED)
                .addHeader(WWW_AUTHENTICATE, challenge())
                .contentType(TEXT)
                .done();
    }

    private String challenge() {
        return String.format("Basic realm=\"%s\"", realm);
    }
}
