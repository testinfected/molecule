package com.vtence.molecule.middlewares;

import com.vtence.molecule.Request;
import com.vtence.molecule.Response;
import com.vtence.molecule.http.HttpMethod;
import org.junit.Test;

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
        assertIsNotRedirected(request.secure(true));
    }

    @Test
    public void redirectsToHttpsWhenRequestIsInsecure() throws Exception {
        request.method(GET).serverHost("example.com").uri("/over/there?name=ferret#nose");

        ssl.handle(request, response);

        assertThat(response).hasStatusCode(301)
                            .isRedirectedTo("https://example.com/over/there?name=ferret#nose")
                            .isDone();
    }

    @Test
    public void redirectsToCustomHost() throws Exception {
        request.method(GET).serverHost("example.com").uri("/");

        ssl.redirectTo("ssl.example.com:443").handle(request, response);

        assertThat(response).isRedirectedTo("https://ssl.example.com:443/")
                            .isDone();
    }

    @Test
    public void redirectsPermanentlyOnGetAndHead() throws Exception {
        for (HttpMethod method: asList(HEAD, GET)) {
            request.method(method);

            assertIsRedirectPermanently(request);
        }
    }

    @Test
    public void redirectsTemporaryOnOtherVerbs() throws Exception {
        for (HttpMethod method: asList(OPTIONS, POST, PATCH, PUT, DELETE)) {
            request.method(method);

            assertIsRedirectedTemporary(request);
        }
    }

    @Test
    public void doesNotRedirectProxiedHttps() throws Exception {
        ssl.redirectOn("X-Forwarded-Proto");

        assertIsNotRedirected(request.header("X-Forwarded-Proto", "https"));
        assertIsRedirectedTemporary(request.header("X-Forwarded-Proto", "http"));
    }

    private void assertIsNotRedirected(Request request) throws Exception {
        ssl.handle(request, response);

        assertThat(response).hasNoHeader("Location");
    }

    private void assertIsRedirectPermanently(Request request) throws Exception {
        ssl.handle(request, response);

        assertThat(response).hasStatusCode(301);
    }

    private void assertIsRedirectedTemporary(Request request) throws Exception {
        ssl.handle(request, response);

        assertThat(response).hasStatusCode(307);
    }
}