package com.vtence.molecule;

import org.hamcrest.Matcher;
import org.junit.Test;

import static com.vtence.molecule.http.HttpStatus.NOT_FOUND;
import static com.vtence.molecule.testing.ResponseAssert.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.junit.MatcherAssert.assertThat;

public class MiddlewareStackTest {

    MiddlewareStack stack = new MiddlewareStack();

    @Test public void
    assemblesChainInOrderOfAddition() throws Exception {
        stack.use(middleware("top"));
        stack.use(middleware("middle"));
        stack.use(middleware("bottom"));
        stack.run(application("runner"));

        Response response = stack.boot()
                                 .handle(Request.get("/"));

        assertChainOf(response, is("top -> middle -> bottom -> runner"));
    }

    @Test public void
    supportMountPointsInsteadOfRunners() throws Exception {
        stack.mount("/api", application("api"));

        Response response = stack.boot()
                                 .handle(Request.get("/api"));

        assertChainOf(response, is("api"));
    }

    @Test public void
    usesRunnerAsDefaultMountPoint() throws Exception {
        stack.mount("/api", application("api"));
        stack.run(application("main"));

        Response response = stack.boot()
                                 .handle(Request.get("/"));

        assertChainOf(response, is("main"));
    }

    @Test public void
    mountsToNotFoundInTheAbsenceOfARunner() throws Exception {
        stack.mount("/api", application("api"));

        Response response = stack.boot()
                                 .handle(Request.get("/"));

        assertThat(response).hasStatus(NOT_FOUND);
    }

    @Test public void
    mixesMountPointsAndMiddlewaresAccordingly() throws Exception {
        stack.use(middleware("top"));
        stack.mount("/api", application("api"));
        stack.use(middleware("bottom"));
        stack.mount("/admin", application("admin"));
        stack.run(application("main"));

        Response response = stack.boot()
                                 .handle(Request.get("/"));

        assertChainOf(response, is("top -> bottom -> main"));
    }

    @Test public void
    takesIntoAccountOrderOfMiddlewareAndMountDefinitions() throws Exception {
        stack.use(middleware("top"));
        stack.mount("/api", application("api"));
        stack.use(middleware("bottom"));
        stack.mount("/admin", application("admin"));

        Response response = stack.boot()
                                 .handle(Request.get("/api"));

        assertChainOf(response, is("top -> api"));
    }

    @Test public void
    acceptsAWarmUpSequence() throws Exception {
        final boolean[] booted = {false};

        stack.use(middleware("ready"));
        stack.use(middleware("set"));
        stack.warmup(app -> booted[0] = true);
        stack.run(application("go!"));

        Response response = stack.boot()
                                 .handle(Request.get("/"));

        assertChainOf(response, is("ready -> set -> go!"));
        assertThat("booted", booted[0], is(true));
    }

    @Test(expected = IllegalStateException.class) public void
    eitherMountOrRunnerIsRequired() throws Exception {
        stack.use(middleware("middleware"));

        stack.boot()
             .handle(Request.get("/"));
    }

    private Middleware middleware(final String order) {
        return application -> request -> {
                    Response response = application.handle(request);
                    return response.header("chain", order + " -> " + response.header("chain"));
                };
    }

    private Application application(final String app) {
        return request -> Response.ok().header("chain", app).done();
    }

    private void assertChainOf(Response response, Matcher<? super String> chaining) {
        assertThat(response).hasHeader("chain", chaining);
    }
}
