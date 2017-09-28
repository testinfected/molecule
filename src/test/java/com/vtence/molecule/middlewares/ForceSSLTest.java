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

    @Test
    public void doesNotRedirectRequestsThatAreAlreadySecure() throws Exception {
        assertIsNotRedirected(Request.get("https://example.com"));
    }

    @Test
    public void redirectsToHttpsWhenRequestIsInsecure() throws Exception {
        Response response = ssl.then(ok())
                               .handle(Request.get("http://example.com/over/there?name=ferret#nose"));

        assertThat(response).hasStatusCode(301)
                            .isRedirectedTo("https://example.com/over/there?name=ferret#nose")
                            .isDone();
    }

    @Test
    public void redirectsToCustomHost() throws Exception {
        ssl.redirectTo("ssl.example.com:443");

        Response response = ssl.then(ok())
                               .handle(Request.get("http://example.com"));

        assertThat(response).isRedirectedTo("https://ssl.example.com:443/")
                            .isDone();
    }

    @Test
    public void redirectsPermanentlyOnGetAndHead() throws Exception {
        Request request = Request.get("/");
        for (HttpMethod method : asList(HEAD, GET)) {
            assertIsRedirectedPermanently(request.method(method));
        }
    }

    @Test
    public void redirectsTemporaryOnOtherVerbs() throws Exception {
        Request request = Request.get("/");
        for (HttpMethod method : asList(OPTIONS, POST, PATCH, PUT, DELETE)) {
            assertIsRedirectedTemporary(request.method(method));
        }
    }

    @Test
    public void doesNotRedirectProxiedHttps() throws Exception {
        ssl.redirectOn("X-Forwarded-Proto");

        assertIsNotRedirected(Request.get("/").header("X-Forwarded-Proto", "https"));
        assertIsRedirectedPermanently(Request.get("/").header("X-Forwarded-Proto", "http"));
    }

    @Test
    public void includesHSTSHeaderByDefaultWithOneYearValidity() throws Exception {
        Response response = ssl.then(ok())
                               .handle(Request.get("https://example.com"));

        assertThat(response).hasHeader("Strict-Transport-Security", "max-age=31536000");
    }

    @Test
    public void disablingHSTSHeaderClearsBrowserSettings() throws Exception {
        ssl.hsts(false);
        Response response = ssl.then(ok())
                               .handle(Request.get("https://example.com"));

        assertThat(response).hasHeader("Strict-Transport-Security", "max-age=0");
    }

    @Test
    public void configuresHSTSHeaderExpiry() throws Exception {
        ssl.expires(TimeUnit.DAYS.toSeconds(180));
        Response response = ssl.then(ok())
                               .handle(Request.get("https://example.com"));

        assertThat(response).hasHeader("Strict-Transport-Security", "max-age=15552000");
    }

    @Test
    public void includesSubdomainsInSecurityHeadersIfRequested() throws Exception {
        ssl.includesSubdomains(true);
        Response response = ssl.then(ok())
                               .handle(Request.get("https://example.com"));

        assertThat(response).hasHeader("Strict-Transport-Security", "max-age=31536000; includeSubdomains");
    }

    @Test
    public void prefersAppSecurityHeaders() throws Exception {
        Response response = ssl.then(request -> Response.ok()
                                                        .header("Strict-Transport-Security", "provided"))
                               .handle(Request.get("https://example.com"));

        assertThat(response).hasHeader("Strict-Transport-Security", "provided");
    }

    @Test
    public void canBeDisabledForDevelopmentMode() throws Exception {
        ssl.enable(false);

        assertIsNotRedirected(Request.get("/"));
    }

    private Application ok() {
        return request -> Response.ok().done();
    }

    private void assertIsNotRedirected(Request request) throws Exception {
        Response response = ssl.then(ok()).handle(request);

        assertThat(response).hasNoHeader("Location");
    }

    private void assertIsRedirectedPermanently(Request request) throws Exception {
        Response response = ssl.then(ok())
                               .handle(request);

        assertThat(response).hasStatusCode(301).isDone();
    }

    private void assertIsRedirectedTemporary(Request request) throws Exception {
        Response response = ssl.then(ok()).handle(request);

        assertThat(response).isDone().hasStatusCode(307);
    }
}