package com.vtence.molecule.middlewares;

import com.vtence.molecule.Application;
import com.vtence.molecule.Request;
import com.vtence.molecule.Response;
import com.vtence.molecule.lib.CookieJar;
import org.junit.Test;

import static com.vtence.molecule.testing.RequestAssert.assertThat;
import static com.vtence.molecule.testing.ResponseAssert.assertThat;
import static org.hamcrest.Matchers.contains;

public class CookiesTest {

    Request request = new Request();
    Response response = new Response();

    Cookies cookies = new Cookies();

    @Test
    public void fillsCookieJarWithClientCookies() throws Exception {
        cookies.connectTo(new Application() {
            public void handle(Request request, Response response) throws Exception {
                CookieJar jar = CookieJar.get(request);
                response.body("foo: " + jar.get("foo").value() + ", baz: " + jar.get("baz").value());
            }
        });

        request.addHeader("Cookie", "foo=bar; baz=qux");
        cookies.handle(request, response);
        assertThat(response).hasBodyText("foo: bar, baz: qux");
    }

    @Test
    public void poursNewCookiesFromJar() throws Exception {
        cookies.connectTo(new Application() {
            public void handle(Request request, Response response) throws Exception {
                CookieJar jar = CookieJar.get(request);
                jar.add("oogle", "foogle");
                jar.add("gorp", "mumble");
            }
        });

        request.addHeader("Cookie", "foo=bar; baz=qux");
        cookies.handle(request, response);

        assertThat(response).hasHeaders("Set-Cookie", contains("oogle=foogle; version=1; path=/", "gorp=mumble; version=1; path=/"));
    }

    @Test
    public void expiresDiscardedCookies() throws Exception {
        cookies.connectTo(new Application() {
            public void handle(Request request, Response response) throws Exception {
                CookieJar jar = CookieJar.get(request);
                jar.discard("foo");
            }
        });

        request.addHeader("Cookie", "foo=bar; baz=qux");
        cookies.handle(request, response);

        assertThat(response).hasHeaders("Set-Cookie", contains("foo=; version=1; path=/; max-age=0"));
    }

    @Test public void
    unbindsCookieJarOnceDone() throws Exception {
        cookies.handle(request, response);

        assertThat(request).hasNoAttribute(CookieJar.class);
    }
}
