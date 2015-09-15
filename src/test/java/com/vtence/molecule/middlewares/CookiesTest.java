package com.vtence.molecule.middlewares;

import com.vtence.molecule.Request;
import com.vtence.molecule.Response;
import com.vtence.molecule.lib.CookieJar;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.concurrent.ExecutionException;

import static com.vtence.molecule.testing.RequestAssert.assertThat;
import static com.vtence.molecule.testing.ResponseAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.notNullValue;

public class CookiesTest {

    Request request = new Request();
    Response response = new Response();

    Cookies cookies = new Cookies();
    @Rule
    public ExpectedException error = ExpectedException.none();

    @Test
    public void fillsCookieJarWithClientCookies() throws Exception {
        cookies.connectTo((request, response) -> {
            CookieJar jar = CookieJar.get(request);
            response.body("foo: " + jar.get("foo").value() + ", baz: " + jar.get("baz").value());
        });

        request.addHeader("Cookie", "foo=bar; baz=qux");
        cookies.handle(request, response);
        response.done();

        assertNoExecutionError();
        assertThat(response).hasBodyText("foo: bar, baz: qux");
    }

    @Test
    public void poursNewCookiesFromJar() throws Exception {
        cookies.connectTo((request, response) -> {
            CookieJar jar = CookieJar.get(request);
            jar.add("oogle", "foogle");
            jar.add("gorp", "mumble");
        });

        request.addHeader("Cookie", "foo=bar; baz=qux");
        cookies.handle(request, response);
        response.done();

        assertNoExecutionError();
        assertThat(response).hasHeaders("Set-Cookie",
                contains("oogle=foogle; version=1; path=/", "gorp=mumble; version=1; path=/"));
    }

    @Test
    public void expiresDiscardedCookies() throws Exception {
        cookies.connectTo((request, response) -> {
            CookieJar jar = CookieJar.get(request);
            jar.discard("foo");
        });

        request.addHeader("Cookie", "foo=bar; baz=qux");
        cookies.handle(request, response);
        response.done();

        assertNoExecutionError();
        assertThat(response).hasHeaders("Set-Cookie", contains("foo=; version=1; path=/; max-age=0"));
    }

    @Test
    public void
    unbindsCookieJarOnceDone() throws Exception {
        cookies.handle(request, response);
        assertThat(request).hasAttribute(CookieJar.class, notNullValue());

        response.done();

        assertNoExecutionError();
        assertThat(request).hasNoAttribute(CookieJar.class);
    }

    @Test
    public void
    unbindsCookieJarWhenAnErrorOccurs() throws Exception {
        cookies.connectTo((request, response) -> {
            throw new Exception("Error!");
        });

        error.expectMessage("Error!");
        try {
            cookies.handle(request, response);
        } finally {
            assertThat(request).hasNoAttribute(CookieJar.class);
        }
    }

    @Test
    public void
    unbindsCookieJarWhenAnErrorOccursLater() throws Throwable {
        cookies.handle(request, response);

        response.done(new Exception("Error!"));
        assertThat(request).hasNoAttribute(CookieJar.class);
    }

    private void assertNoExecutionError() throws ExecutionException, InterruptedException {
        response.await();
    }
}
