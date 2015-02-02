package com.vtence.molecule.lib;

import com.vtence.molecule.Application;
import com.vtence.molecule.Request;
import com.vtence.molecule.Response;
import org.junit.Test;

import static com.vtence.molecule.support.ResponseAssertions.assertThat;
import static org.hamcrest.CoreMatchers.is;

public class MiddlewareStackTest {

    MiddlewareStack stack = new MiddlewareStack();

    Request request = new Request();
    Response response = new Response();

    @Test public void
    assemblesChainInOrderOfAddition() throws Exception {
        stack.use(middleware("top"));
        stack.use(middleware("middle"));
        stack.use(middleware("bottom"));
        stack.run(application("app"));

        stack.handle(request, response);
        assertChain(is("top -> middle -> bottom -> app"));
    }

    private Middleware middleware(final String order) {
        return new AbstractMiddleware() {
            public void handle(Request request, Response response) throws Exception {
                forward(request, response);
                response.header("chain", order + " -> " + response.header("chain"));
            }
        };
    }

    private Application application(final String app) {
        return new Application() {
            public void handle(Request request, Response response) throws Exception {
                response.header("chain", app);
            }
        };
    }

    private void assertChain(org.hamcrest.Matcher<? super String> chaining) {
        assertThat(response).hasHeader("chain", chaining);
    }
}
