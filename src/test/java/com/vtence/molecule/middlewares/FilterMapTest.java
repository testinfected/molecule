package com.vtence.molecule.middlewares;

import com.vtence.molecule.Application;
import com.vtence.molecule.Request;
import com.vtence.molecule.Response;
import com.vtence.molecule.lib.matchers.Matcher;
import com.vtence.molecule.lib.matchers.Matchers;
import org.junit.Before;
import org.junit.Test;

import static com.vtence.molecule.testing.ResponseAssert.assertThat;
import static java.lang.String.format;

public class FilterMapTest {

    FilterMap filters = new FilterMap();

    Request request = new Request();
    Response response = new Response();

    @Before public void
    stubApplication() {
        filters.connectTo(new Application() {
            public void handle(Request request, Response response) throws Exception {
                response.header("content", "content");
            }
        });
    }

    @Test public void
    immediatelyForwardsRequestWhenNoFilterIsRegistered() throws Exception {
        filters.handle(request, response);
        assertFilteredContent("content");
    }

    @Test public void
    runsRequestThroughMatchingFilter() throws Exception {
        filters.map(none(), filter("none"));
        filters.map(all(), filter("filter"));

        filters.handle(request, response);
        assertFilteredContent("filter(content)");
    }

    @Test public void
    forwardsRequestIfNoFilterMatches() throws Exception {
        filters.map(none(), filter("no"));
        filters.handle(request, response);
        assertFilteredContent("content");
    }

    @Test public void
    matchesOnPathPrefix() throws Exception {
        request.path("/filtered/path");
        filters.map("/filtered", filter("filter"));

        filters.handle(request, response);
        assertFilteredContent("filter(content)");
    }

    @Test public void
    appliesLastRegisteredOfMatchingFilters() throws Exception {
        filters.map(all(), filter("filter"));
        filters.map(all(), filter("replacement"));

        filters.handle(request, response);
        assertFilteredContent("replacement(content)");
    }

    private void assertFilteredContent(String content) {
        assertThat(response).hasHeader("content", content);
    }

    private Matcher<Request> all() {
        return Matchers.anything();
    }

    private Matcher<Request> none() {
        return Matchers.nothing();
    }

    private AbstractMiddleware filter(final String name) {
        return new AbstractMiddleware() {
            public void handle(Request request, Response response) throws Exception {
                forward(request, response);
                response.header("content", format("%s(%s)", name, response.header("content")));
            }
        };
    }
}