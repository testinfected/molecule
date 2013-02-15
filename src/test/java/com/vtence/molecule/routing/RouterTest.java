package com.vtence.molecule.routing;

import com.vtence.molecule.Application;
import com.vtence.molecule.HttpMethod;
import com.vtence.molecule.Request;
import com.vtence.molecule.Response;
import com.vtence.molecule.middlewares.Routes;
import com.vtence.molecule.support.MockResponse;
import org.junit.Test;

import static com.vtence.molecule.routing.RouterTest.Echo.echo;
import static com.vtence.molecule.support.MockRequest.DELETE;
import static com.vtence.molecule.support.MockRequest.GET;
import static com.vtence.molecule.support.MockRequest.POST;
import static com.vtence.molecule.support.MockRequest.PUT;

public class RouterTest {

    @Test public void
    opensRoutesMatchingSpecifiedPathsAndVerbs() throws Exception {
        Routes routes = Routes.draw(new Router() {{
            map("/uri").via(HttpMethod.POST).to(echo("post to /uri"));
            map("/other/uri").via(HttpMethod.GET).to(echo("get to /other/uri"));
        }}).defaultsTo(echo("not matched"));

        dispatch(routes, GET("/other/uri")).assertBody("get to /other/uri");
        dispatch(routes, POST("/uri")).assertBody("post to /uri");
    }

    @Test public void
    providesConvenientShortcutsForDrawingRoutesUsingStandardVerbs() throws Exception {
        Routes routes = Routes.draw(new Router() {{
            get("/").to(echo("get"));
            put("/").to(echo("put"));
            post("/").to(echo("post"));
            delete("/").to(echo("delete"));
        }}).defaultsTo(echo("not matched"));

        dispatch(routes, GET("/")).assertBody("get");
        dispatch(routes, POST("/")).assertBody("post");
        dispatch(routes, PUT("/")).assertBody("put");
        dispatch(routes, DELETE("/")).assertBody("delete");
    }

    @Test public void
    drawsRoutesInOrder() throws Exception {
        Routes routes = Routes.draw(new Router() {{
            map("/").via(HttpMethod.GET).to(echo("get /"));
            map("/").to(echo("any /"));
        }}).defaultsTo(echo("not matched"));

        dispatch(routes, GET("/"));
    }

    private MockResponse dispatch(Routes routes, Request request) throws Exception {
        MockResponse response = new MockResponse();
        routes.handle(request, response);
        return response;
    }

    public static class Echo implements Application {

        private final String message;

        public static Application echo(String message) {
            return new Echo(message);
        }

        public Echo(String message) {
            this.message = message;
        }

        public void handle(Request request, Response response) throws Exception {
            response.body(message);
        }
    }
}
