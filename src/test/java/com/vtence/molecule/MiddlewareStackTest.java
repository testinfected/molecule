package com.vtence.molecule;

import com.vtence.molecule.middlewares.AbstractMiddleware;
import org.junit.Test;

import static com.vtence.molecule.testing.ResponseAssert.assertThat;
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
        stack.run(application("runner"));

        stack.handle(request, response);
        assertChain(is("top -> middle -> bottom -> runner"));
    }

    @Test(expected = IllegalStateException.class) public void
    reportsIllegalStateIfNoRunnerWasSpecified() throws Exception {
        stack.use(middleware("middleware"));

        stack.handle(request, response);
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
        return (request, response) -> response.header("chain", app);
    }

    private void assertChain(org.hamcrest.Matcher<? super String> chaining) {
        assertThat(response).hasHeader("chain", chaining);
    }
}