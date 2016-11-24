package com.vtence.molecule.middlewares;

import com.vtence.molecule.Request;
import com.vtence.molecule.Response;
import com.vtence.molecule.http.HttpStatus;

import static com.vtence.molecule.http.HttpMethod.GET;
import static com.vtence.molecule.http.HttpMethod.HEAD;
import static com.vtence.molecule.http.HttpStatus.MOVED_PERMANENTLY;
import static com.vtence.molecule.http.HttpStatus.TEMPORARY_REDIRECT;

public class ForceSSL extends AbstractMiddleware {
    private String customHost;
    private String redirectOn;

    public ForceSSL redirectTo(String host) {
        this.customHost = host;
        return this;
    }

    public void redirectOn(String header) {
        redirectOn = header;
    }

    public void handle(Request request, Response response) throws Exception {
        if (!secure(request)) {
            redirectToHttps(request, response);
        } else {
            forward(request, response);
        }
    }

    private boolean secure(Request request) {
        return request.secure() || isProxiedHttps(request);
    }

    private boolean isProxiedHttps(Request request) {
        return redirectOn != null && "https".equalsIgnoreCase(request.header(redirectOn));
    }

    private void redirectToHttps(Request request, Response response) {
        response.redirectTo(httpsLocationFor(request))
                .status(redirectionStatusFor(request))
                .done();
    }

    private String httpsLocationFor(Request request) {
        String host = customHost != null ? customHost : request.hostname();
        return "https://" + host + request.uri();
    }

    private HttpStatus redirectionStatusFor(Request request) {
        return request.method() == GET || request.method() == HEAD ? MOVED_PERMANENTLY : TEMPORARY_REDIRECT;
    }
}
