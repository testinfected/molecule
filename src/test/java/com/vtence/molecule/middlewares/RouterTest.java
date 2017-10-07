package com.vtence.molecule.middlewares;

import com.vtence.molecule.Application;
import com.vtence.molecule.Request;
import com.vtence.molecule.Response;
import org.junit.Test;

import static com.vtence.molecule.http.HttpStatus.NOT_FOUND;
import static com.vtence.molecule.lib.predicates.Predicates.all;
import static com.vtence.molecule.lib.predicates.Predicates.none;
import static com.vtence.molecule.testing.ResponseAssert.assertThat;

public class RouterTest {
    Router router = new Router();

    @Test public void
    rendersNotFoundWhenNoRouteMatch() throws Exception {
        router.add(new StaticRoute(none(), echo("other")));
        Response response = router.handle(Request.get("/"));
        assertThat(response).hasStatus(NOT_FOUND);
    }

    @Test public void
    dispatchesToFirstRouteThatMatches() throws Exception {
        router.add(new StaticRoute(all(), echo("preferred")));
        router.add(new StaticRoute(all(), echo("alternate")));
        Response response = router.handle(Request.get("/"));
        assertThat(response).hasBodyText("preferred");
    }

    private Application echo(final String text) {
        return request -> Response.ok().done(text);
    }

}