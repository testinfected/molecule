package com.vtence.molecule.middlewares;

import com.vtence.molecule.Application;
import com.vtence.molecule.Middleware;
import com.vtence.molecule.Request;
import com.vtence.molecule.Response;
import org.junit.Test;

import static com.vtence.molecule.lib.predicates.Predicates.all;
import static com.vtence.molecule.lib.predicates.Predicates.none;
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
        filters.map(none(), filter("none"));
        filters.map(all(), filter("filter"));

        Response response = filters.then(stubResponse("content"))
                                   .handle(Request.get("/"));
        assertFilteredContent(response, "filter(content)");
    }

    @Test public void
    forwardsRequestIfNoFilterMatch() throws Exception {
        filters.map(none(), filter("no"));
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
        filters.map(all(), filter("filter"));
        filters.map(all(), filter("replacement"));

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