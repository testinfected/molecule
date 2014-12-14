package com.vtence.molecule.routing;

import com.vtence.molecule.Application;
import com.vtence.molecule.Request;
import com.vtence.molecule.Response;
import com.vtence.molecule.http.HttpMethod;
import com.vtence.molecule.middlewares.Router;
import com.vtence.molecule.support.MockResponse;
import org.junit.Test;

import static com.vtence.molecule.routing.DynamicRoutesTest.Echo.echo;
import static com.vtence.molecule.support.MockRequest.*;

public class DynamicRoutesTest {

    @Test public void
    routesToDefinitionMatchingRequestPathAndVerb() throws Exception {
        Router router = Router.draw(new DynamicRoutes() {{
            map("/uri").via(HttpMethod.POST).to(echo("post to /uri"));
            map("/other/uri").via(HttpMethod.GET).to(echo("get to /other/uri"));
        }}).defaultsTo(echo("not matched"));

        dispatch(router, POST("/uri")).assertBody("post to /uri");
        dispatch(router, GET("/uri")).assertBody("not matched");
        dispatch(router, GET("/other/uri")).assertBody("get to /other/uri");
    }

    @Test public void
    providesConvenientShortcutsForDrawingRoutesUsingStandardVerbs() throws Exception {
        Router router = Router.draw(new DynamicRoutes() {{
            get("/").to(echo("get"));
            post("/").to(echo("post"));
            put("/").to(echo("put"));
            delete("/").to(echo("delete"));
            patch("/").to(echo("patch"));
            head("/").to(echo("head"));
            options("/").to(echo("options"));
        }});

        dispatch(router, GET("/")).assertBody("get");
        dispatch(router, POST("/")).assertBody("post");
        dispatch(router, PUT("/")).assertBody("put");
        dispatch(router, DELETE("/")).assertBody("delete");
        dispatch(router, PATCH("/")).assertBody("patch");
        dispatch(router, HEAD("/")).assertBody("head");
        dispatch(router, OPTIONS("/")).assertBody("options");
    }

    @Test public void
    matchesRoutesInDefinitionOrder() throws Exception {
        Router router = Router.draw(new DynamicRoutes() {{
            map("/").via(HttpMethod.GET).to(echo("original"));
            map("/").to(echo("override"));
        }});

        dispatch(router, GET("/")).assertBody("original");
    }

    @Test
    public void
    matchesAnyOfTheVerbsSpecifiedInViaClause() throws Exception {
        Router router = Router.draw(new DynamicRoutes() {{
            map("/").via(HttpMethod.POST, HttpMethod.PUT).to(echo("found"));
        }});

        dispatch(router, POST("/")).assertBody("found");
        dispatch(router, PUT("/")).assertBody("found");
    }

    @Test public void
    extractParametersBoundToPath() throws Exception {
        Router router = Router.draw(new DynamicRoutes() {{
            map("/resource/:id").to(echo("parameters"));
        }});

        dispatch(router, GET("/resource/42")).assertBody("parameters {id=[42]}");
    }

    private MockResponse dispatch(Router router, Request request) throws Exception {
        MockResponse response = new MockResponse();
        router.handle(request, response);
        return response;
    }

    public static class Echo implements Application {
        private final String message;

        public Echo(String message) {
            this.message = message;
        }

        public static Application echo(String message) {
            return new Echo(message);
        }

        public void handle(Request request, Response response) throws Exception {
            response.body(message + (request.parameters().isEmpty() ? "" : " " + request.parameters()));
        }
    }
}
