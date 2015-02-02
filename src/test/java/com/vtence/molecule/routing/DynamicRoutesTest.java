package com.vtence.molecule.routing;

import com.vtence.molecule.Application;
import com.vtence.molecule.Request;
import com.vtence.molecule.Response;
import com.vtence.molecule.http.HttpMethod;
import com.vtence.molecule.middlewares.Router;
import org.junit.Test;

import static com.vtence.molecule.routing.DynamicRoutesTest.Echo.echo;
import static com.vtence.molecule.support.MockRequest.*;
import static com.vtence.molecule.support.ResponseAssertions.assertThat;

public class DynamicRoutesTest {

    @Test public void
    routesToDefinitionMatchingRequestPathAndVerb() throws Exception {
        Router router = Router.draw(new DynamicRoutes() {{
            map("/uri").via(HttpMethod.POST).to(echo("post to /uri"));
            map("/other/uri").via(HttpMethod.GET).to(echo("get to /other/uri"));
        }}).defaultsTo(echo("not matched"));

        assertThat(dispatch(router, POST("/uri"))).hasBodyText("post to /uri");
        assertThat(dispatch(router, GET("/uri"))).hasBodyText("not matched");
        assertThat(dispatch(router, GET("/other/uri"))).hasBodyText("get to /other/uri");
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

        assertThat(dispatch(router, GET("/"))).hasBodyText("get");
        assertThat(dispatch(router, POST("/"))).hasBodyText("post");
        assertThat(dispatch(router, PUT("/"))).hasBodyText("put");
        assertThat(dispatch(router, DELETE("/"))).hasBodyText("delete");
        assertThat(dispatch(router, PATCH("/"))).hasBodyText("patch");
        assertThat(dispatch(router, HEAD("/"))).hasBodyText("head");
        assertThat(dispatch(router, OPTIONS("/"))).hasBodyText("options");
    }

    @Test public void
    matchesRoutesInDefinitionOrder() throws Exception {
        Router router = Router.draw(new DynamicRoutes() {{
            map("/").via(HttpMethod.GET).to(echo("original"));
            map("/").to(echo("override"));
        }});

        assertThat(dispatch(router, GET("/"))).hasBodyText("original");
    }

    @Test
    public void
    matchesAnyOfTheVerbsSpecifiedInViaClause() throws Exception {
        Router router = Router.draw(new DynamicRoutes() {{
            map("/").via(HttpMethod.POST, HttpMethod.PUT).to(echo("found"));
        }});

        assertThat(dispatch(router, POST("/"))).hasBodyText("found");
        assertThat(dispatch(router, PUT("/"))).hasBodyText("found");
    }

    @Test public void
    extractParametersBoundToPath() throws Exception {
        Router router = Router.draw(new DynamicRoutes() {{
            map("/resource/:id").to(echo("parameters"));
        }});

        assertThat(dispatch(router, GET("/resource/42"))).hasBodyText("parameters {id=[42]}");
    }

    private Response dispatch(Router router, Request request) throws Exception {
        Response response = new Response();
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
            response.body(message + (request.allParameters().isEmpty() ? "" : " " + request.allParameters()));
        }
    }
}