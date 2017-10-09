package com.vtence.molecule.middlewares;

import com.vtence.molecule.Application;
import com.vtence.molecule.Middleware;
import com.vtence.molecule.Request;
import com.vtence.molecule.Response;
import org.junit.Test;

import static com.vtence.molecule.lib.predicates.Predicates.anything;
import static com.vtence.molecule.lib.predicates.Predicates.nothing;
import static com.vtence.molecule.testing.ResponseAssert.assertThat;
import static java.lang.String.format;

public class FilterMapTest {

    FilterMap filters = new FilterMap();

    @Test public void
    immediatelyForwardsRequestWhenNoFilterIsRegistered() throws Exception {
        Response response = filters.then(stubResponse("content"))
                                   .handle(Request.get("/"));
        assertFilteredContent(response, "content");
    }

    @Test public void
    runsRequestThroughMatchingFilter() throws Exception {
        filters.map(nothing(), filter("none"));
        filters.map(anything(), filter("filter"));

        Response response = filters.then(stubResponse("content"))
                                   .handle(Request.get("/"));
        assertFilteredContent(response, "filter(content)");
    }

    @Test public void
    forwardsRequestIfNoFilterMatch() throws Exception {
        filters.map(nothing(), filter("no"));
        Response response = filters.then(stubResponse("content"))
                                   .handle(Request.get("/"));
        assertFilteredContent(response, "content");
    }

    @Test public void
    matchesOnPathPrefix() throws Exception {
        filters.map("/filtered", filter("filter"));

        Response response = filters.then(stubResponse("content"))
                                   .handle(Request.get("/filtered/path"));
        assertFilteredContent(response, "filter(content)");
    }

    @Test public void
    appliesLastRegisteredOfMatchingFilters() throws Exception {
        filters.map(anything(), filter("filter"));
        filters.map(anything(), filter("replacement"));

        Response response = filters.then(stubResponse("content"))
                                   .handle(Request.get("/"));
        assertFilteredContent(response, "replacement(content)");
    }

    private void assertFilteredContent(Response response, String content) {
        assertThat(response).hasHeader("content", content);
    }

    private Middleware filter(final String name) {
        return next -> request -> {
            Response response = next.handle(request);
            return response.header("content", format("%s(%s)", name, response.header("content")));
        };
    }

    private Application stubResponse(String content) {
        return request -> Response.ok().header("content", content).done();
    }
};