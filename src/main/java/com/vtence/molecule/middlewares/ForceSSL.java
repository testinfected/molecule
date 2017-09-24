package com.vtence.molecule.middlewares;

import com.vtence.molecule.Application;
import com.vtence.molecule.Request;
import com.vtence.molecule.Response;
import com.vtence.molecule.http.HttpStatus;

import java.util.concurrent.TimeUnit;

import static com.vtence.molecule.http.HeaderNames.STRICT_TRANSPORT_SECURITY;
import static com.vtence.molecule.http.HttpMethod.GET;
import static com.vtence.molecule.http.HttpMethod.HEAD;
import static com.vtence.molecule.http.HttpStatus.MOVED_PERMANENTLY;
import static com.vtence.molecule.http.HttpStatus.TEMPORARY_REDIRECT;

public class ForceSSL extends AbstractMiddleware {

    private boolean enable;

    // Default HSTS settings
    private boolean hsts = true;
    private long hstsExpiry = TimeUnit.DAYS.toSeconds(365);

    private boolean subdomains;
    private String customHost;
    private String redirectOn;

    public ForceSSL() {
        this(true);
    }

    public ForceSSL(boolean enabled) {
        this.enable = enabled;
    }

    public void enable(boolean enabled) {
        enable = enabled;
    }

    public void hsts(boolean enabled) {
        hsts = enabled;
    }

    public void expires(long delayInSeconds) {
        hstsExpiry = delayInSeconds;
    }

    public void includesSubdomains(boolean included) {
        subdomains = included;
    }

    public ForceSSL redirectTo(String host) {
        this.customHost = host;
        return this;
    }

    public void redirectOn(String header) {
        redirectOn = header;
    }

    public Application then(Application next) {
        return Application.of(request -> {
            if (enable && !secure(request)) {
                return redirectToHttps(request);
            } else {
                return next.handle(request).whenSuccessful(this::addHSTSHeader);
            }
        });
    }

    public void handle(Request request, Response response) throws Exception {
        if (enable && !secure(request)) {
            redirectToHttps(request, response);
        } else {
            forward(request, response).whenSuccessful(this::addHSTSHeader);
        }
    }

    private void addHSTSHeader(Response response) {
        if (response.hasHeader(STRICT_TRANSPORT_SECURITY)) return;

        if (!hsts) {
            hstsExpiry = 0;
        }

        String security = "max-age=" + hstsExpiry + (subdomains ? "; includeSubdomains" : "");
        response.header(STRICT_TRANSPORT_SECURITY, security);
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

    private Response redirectToHttps(Request request) {
        return Response.redirect(httpsLocationFor(request), redirectionStatusFor(request))
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
