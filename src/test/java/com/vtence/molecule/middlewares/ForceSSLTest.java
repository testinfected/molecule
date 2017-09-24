package com.vtence.molecule.middlewares;

import com.vtence.molecule.Application;
import com.vtence.molecule.Request;
import com.vtence.molecule.Response;
import com.vtence.molecule.http.HttpMethod;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static com.vtence.molecule.http.HttpMethod.DELETE;
import static com.vtence.molecule.http.HttpMethod.GET;
import static com.vtence.molecule.http.HttpMethod.HEAD;
import static com.vtence.molecule.http.HttpMethod.OPTIONS;
import static com.vtence.molecule.http.HttpMethod.PATCH;
import static com.vtence.molecule.http.HttpMethod.POST;
import static com.vtence.molecule.http.HttpMethod.PUT;
import static com.vtence.molecule.testing.ResponseAssert.assertThat;
import static java.util.Arrays.asList;

public class ForceSSLTest {

    ForceSSL ssl = new ForceSSL();

    Request request = new Request();
    Response response = new Response();

    @Test
    public void doesNotRedirectRequestsThatAreAlreadySecure() throws Exception {
        assertIsNotRedirected(secure(request));
    }

    @Test
    public void redirectsToHttpsWhenRequestIsInsecure() throws Exception {
        Response response = ssl.then(ok())
                               .handle(Request.get("/over/there?name=ferret#nose")
                                              .secure(false)
                                              .serverHost("example.com"));

        assertThat(response).hasStatusCode(301)
                            .isRedirectedTo("https://example.com/over/there?name=ferret#nose")
                            .isDone();
    }

    @Test
    public void redirectsToCustomHost() throws Exception {
        ssl.redirectTo("ssl.example.com:443");

        Response response = ssl.then(ok())
                               .handle(Request.get("/")
                                              .serverHost("example.com"));

        assertThat(response).isRedirectedTo("https://ssl.example.com:443/")
                            .isDone();
    }

    @Test
    public void redirectsPermanentlyOnGetAndHead() throws Exception {
        Request request = new Request();
        for (HttpMethod method : asList(HEAD, GET)) {
            assertIsRedirectPermanently(request.method(method));
        }
    }

    @Test
    public void redirectsTemporaryOnOtherVerbs() throws Exception {
        Request request = new Request();
        for (HttpMethod method : asList(OPTIONS, POST, PATCH, PUT, DELETE)) {
            assertIsRedirectedTemporary(request.method(method));
        }
    }

    @Test
    public void doesNotRedirectProxiedHttps() throws Exception {
        ssl.redirectOn("X-Forwarded-Proto");

        assertIsNotRedirected(request.header("X-Forwarded-Proto", "https"));
        assertIsRedirectedTemporary(request.header("X-Forwarded-Proto", "http"));
    }

    @Test
    public void includesHSTSHeaderByDefaultWithOneYearValidity() throws Exception {
        Response response = ssl.then(ok())
                               .handle(secure(request));

        assertThat(response).hasHeader("Strict-Transport-Security", "max-age=31536000");
    }

    @Test
    public void disablingHSTSHeaderClearsBrowserSettings() throws Exception {
        ssl.hsts(false);
        Response response = ssl.then(ok())
                               .handle(secure(request));

        assertThat(response).hasHeader("Strict-Transport-Security", "max-age=0");
    }

    @Test
    public void configuresHSTSHeaderExpiry() throws Exception {
        ssl.expires(TimeUnit.DAYS.toSeconds(180));
        Response response = ssl.then(ok())
                               .handle(secure(request));

        assertThat(response).hasHeader("Strict-Transport-Security", "max-age=15552000");
    }

    @Test
    public void includesSubdomainsInSecurityHeadersIfRequested() throws Exception {
        ssl.includesSubdomains(true);
        Response response = ssl.then(ok())
                               .handle(secure(request));

        assertThat(response).hasHeader("Strict-Transport-Security", "max-age=31536000; includeSubdomains");
    }

    @Test
    public void prefersAppSecurityHeaders() throws Exception {
        Response response = ssl.then(request -> Response.ok()
                                                        .header("Strict-Transport-Security", "provided"))
                               .handle(secure(request));

        assertThat(response).hasHeader("Strict-Transport-Security", "provided");
    }

    @Test
    public void canBeDisabledForDevelopmentMode() throws Exception {
        ssl.enable(false);

        assertIsNotRedirected(request);
    }

    private Application.ApplicationFunction ok() {
        return request -> Response.ok().done();
    }

    private void assertIsNotRedirected(Request request) throws Exception {
        Response response = ssl.then(ok()).handle(request);

        assertThat(response).hasNoHeader("Location");
    }

    private void assertIsRedirectPermanently(Request request) throws Exception {
        Response response = ssl.then(ok())
                               .handle(request);

        assertThat(response).hasStatusCode(301).isDone();
    }

    private void assertIsRedirectedTemporary(Request request) throws Exception {
        Response response = ssl.then(ok()).handle(request);

        assertThat(response).isDone().hasStatusCode(307);
    }

    private Request secure(Request request) {
        return request.secure(true);
    }
}