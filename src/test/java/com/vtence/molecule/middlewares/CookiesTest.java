package com.vtence.molecule.middlewares;

import com.vtence.molecule.Application;
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

    @Rule
    public ExpectedException error = ExpectedException.none();

    Cookies cookies = new Cookies();

    @Test
    public void fillsCookieJarWithClientCookies() throws Exception {
        Response response = cookies.then(request -> {
            CookieJar jar = CookieJar.get(request);
            return Response.ok()
                           .done("foo: " + jar.get("foo").value() + ", " +
                                 "baz: " + jar.get("baz").value());
        }).handle(Request.get("/")
                         .addHeader("Cookie", "foo=bar; baz=qux"));

        assertNoExecutionError(response);
        assertThat(response).hasBodyText("foo: bar, baz: qux");
    }

    @Test
    public void poursNewCookiesFromJar() throws Exception {
        Response response = cookies.then(request -> {
            CookieJar jar = CookieJar.get(request);
            jar.add("oogle", "foogle");
            jar.add("gorp", "mumble");
            return Response.ok().done();
        }).handle(Request.get("/")
                         .addHeader("Cookie", "foo=bar; baz=qux"));

        assertNoExecutionError(response);
        assertThat(response).hasHeaders("Set-Cookie",
                                        contains("oogle=foogle; version=1; path=/", "gorp=mumble; version=1; path=/"));
    }

    @Test
    public void expiresDiscardedCookies() throws Exception {
        Response response = cookies.then(request -> {
            CookieJar jar = CookieJar.get(request);
            jar.discard("foo");
            return Response.ok().done();
        }).handle(Request.get("/")
                         .addHeader("Cookie", "foo=bar; baz=qux"));

        assertNoExecutionError(response);
        assertThat(response).hasHeaders("Set-Cookie", contains("foo=; version=1; path=/; max-age=0"));
    }

    @Test
    public void
    unbindsCookieJarOnceDone() throws Exception {
        Request request = Request.get("/");
        Response response = cookies.then(ok()).handle(request);

        assertThat(request).hasAttribute(CookieJar.class, notNullValue());
        response.done();

        assertNoExecutionError(response);
        assertThat(request).hasNoAttribute(CookieJar.class);
    }

    @Test
    public void
    unbindsCookieJarWhenAnErrorOccurs() throws Exception {
        error.expectMessage("Error!");

        Request request = Request.get("/");
        try {
            cookies.then(crash()).handle(request);
        } finally {
            assertThat(request).hasNoAttribute(CookieJar.class);
        }
    }

    @Test
    public void
    unbindsCookieJarWhenAnErrorOccursLater() throws Throwable {
        Request request = Request.get("/");
        Response response = cookies.then(ok()).handle(request);

        response.done(new Exception("Error!"));
        assertThat(request).hasNoAttribute(CookieJar.class);
    }

    private Application ok() {
        return request -> Response.ok();
    }

    private Application crash() {
        return request -> {
            throw new Exception("Error!");
        };
    }

    private void assertNoExecutionError(Response response) throws ExecutionException, InterruptedException {
        response.await();
    }
}
