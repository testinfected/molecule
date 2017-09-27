package com.vtence.molecule.middlewares;

import com.vtence.molecule.Application;
import com.vtence.molecule.Request;
import com.vtence.molecule.Response;
import com.vtence.molecule.lib.matchers.Anything;
import com.vtence.molecule.lib.matchers.Matcher;
import com.vtence.molecule.lib.matchers.Nothing;
import com.vtence.molecule.routing.Route;
import org.junit.Test;

import static com.vtence.molecule.http.HttpStatus.NOT_FOUND;
import static com.vtence.molecule.testing.ResponseAssert.assertThat;

public class RouterTest {

    Router router = new Router();

    @Test public void
    rendersNotFoundWhenNoRouteMatch() throws Exception {
        router.add(new StaticRoute(none(), route("other")));
        Response response = router.handle(Request.get("/"));
        assertThat(response).hasStatus(NOT_FOUND);
    }

    @Test public void
    dispatchesToFirstRouteThatMatches() throws Exception {
        router.add(new StaticRoute(all(), route("preferred")));
        router.add(new StaticRoute(all(), route("alternate")));
        Response response = router.handle(Request.get("/"));
        assertThat(response).hasBodyText("preferred");
    }

    private Application route(final String name) {
        return request -> Response.ok().done(name);
    }

    public static Matcher<Request> all() {
        return new Anything<>();
    }

    public static Matcher<Request> none() {
        return new Nothing<>();
    }

    private class StaticRoute implements Route {
        private final Matcher<Request> requestMatcher;
        private final Application app;

        public StaticRoute(Matcher<Request> requestMatcher, Application app) {
            this.requestMatcher = requestMatcher;
            this.app = app;
        }

        public Response handle(Request request) throws Exception {
            return app.handle(request);
        }

        public boolean matches(Request actual) {
            return requestMatcher.matches(actual);
        }
    }
}