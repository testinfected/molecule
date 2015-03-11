package com.vtence.molecule.middlewares;

import com.vtence.molecule.Application;
import com.vtence.molecule.Request;
import com.vtence.molecule.Response;
import com.vtence.molecule.lib.CookieJar;
import org.junit.Test;

import static com.vtence.molecule.http.HeaderNames.COOKIE;
import static com.vtence.molecule.testing.ResponseAssert.assertThat;

public class CookiesTest {

    Request request = new Request();
    Response response = new Response();

    Cookies cookies = new Cookies();

    @Test
    public void readsRequestCookies() throws Exception {
        cookies.connectTo(new Application() {
            public void handle(Request request, Response response) throws Exception {
                CookieJar jar = CookieJar.get(request);
                response.body("foo: " + jar.get("foo").value() + ", baz: " + jar.get("baz").value());
            }
        });

        request.addHeader(COOKIE, "foo=bar; baz=qux");
        cookies.handle(request, response);
        assertThat(response).hasBodyText("foo: bar, baz: qux");
    }

    public void unbindsCookieJarAfterwards() {}
}
