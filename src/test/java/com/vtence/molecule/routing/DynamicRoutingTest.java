package com.vtence.molecule.routing;

import com.vtence.molecule.Application;
import com.vtence.molecule.Request;
import com.vtence.molecule.Response;
import com.vtence.molecule.http.HttpMethod;
import com.vtence.molecule.middlewares.Router;
import org.junit.Test;

import static com.vtence.molecule.http.HeaderNames.ACCEPT;
import static com.vtence.molecule.http.HttpMethod.GET;
import static com.vtence.molecule.http.HttpMethod.POST;
import static com.vtence.molecule.lib.predicates.Predicates.anything;
import static com.vtence.molecule.routing.DynamicRoutingTest.Echo.echo;
import static com.vtence.molecule.testing.ResponseAssert.assertThat;

public class DynamicRoutingTest {

    @Test public void
    matchingRequestPathAndVerb() throws Exception {
        Router router = Router.draw(new Routes() {{
            map("/uri").via(POST).to(echo("post to /uri"));
            map("/other/uri").via(GET).to(echo("get to /other/uri"));

            map(anything()).to(echo("not matched"));
        }});

        assertThat(router.handle(Request.post("/uri"))).hasBodyText("post to /uri");
        assertThat(router.handle(Request.get("/uri"))).hasBodyText("not matched");
        assertThat(router.handle(Request.get("/other/uri"))).hasBodyText("get to /other/uri");
    }

    @Test public void
    usingConvenientShortcutsForDrawingRoutesUsingStandardVerbs() throws Exception {
        Router router = Router.draw(new Routes() {{
            get("/").to(echo("get"));
            post("/").to(echo("post"));
            put("/").to(echo("put"));
            delete("/").to(echo("delete"));
            patch("/").to(echo("patch"));
            head("/").to(echo("head"));
            options("/").to(echo("options"));
        }});

        assertThat(router.handle(Request.get("/"))).hasBodyText("get");
        assertThat(router.handle(Request.post("/"))).hasBodyText("post");
        assertThat(router.handle(Request.put("/"))).hasBodyText("put");
        assertThat(router.handle(Request.delete("/"))).hasBodyText("delete");
        assertThat(router.handle(Request.patch("/"))).hasBodyText("patch");
        assertThat(router.handle(Request.head("/"))).hasBodyText("head");
        assertThat(router.handle(Request.options("/"))).hasBodyText("options");
    }

    @Test public void
    routesMatchInOrder() throws Exception {
        Router router = Router.draw(new Routes() {{
            map("/").via(GET).to(echo("original"));
            map("/").to(echo("override"));
        }});

        assertThat(router.handle(Request.get("/"))).hasBodyText("original");
    }

    @Test
    public void
    matchingAnyOfTheVerbsSpecifiedInViaClause() throws Exception {
        Router router = Router.draw(new Routes() {{
            map("/").via(POST, HttpMethod.PUT).to(echo("found"));
        }});

        assertThat(router.handle(Request.post("/"))).hasBodyText("found");
        assertThat(router.handle(Request.put("/"))).hasBodyText("found");
    }

    @Test
    public void
    matchingOnAcceptHeader() throws Exception {
        Router router = Router.draw(new Routes() {{
            get("/").accept("application/json").to(echo("json"));
            get("/").accept("image/*").to(echo("image"));
            get("/").to(echo("html"));
        }});

        assertThat(router.handle(Request.get("/").header(ACCEPT, "application/json"))).hasBodyText("json");
        assertThat(router.handle(Request.get("/").header(ACCEPT, "image/png"))).hasBodyText("image");
        assertThat(router.handle(Request.get("/").header(ACCEPT, "text/html"))).hasBodyText("html");
    }

    @Test public void
    extractingParametersBoundToPath() throws Exception {
        Router router = Router.draw(new Routes() {{
            map("/resource/:id").to(echo("parameters"));
        }});

        assertThat(router.handle(Request.get("/resource/42"))).hasBodyText("parameters {id=[42]}");
    }

    public static class Echo implements Application {
        private final String message;

        public Echo(String message) {
            this.message = message;
        }

        public static Application echo(String message) {
            return new Echo(message);
        }

        public Response handle(Request request) throws Exception {
            return Response.ok().body(message + (request.allParameters().isEmpty() ? "" : " " + request.allParameters()));
        }
    }
}