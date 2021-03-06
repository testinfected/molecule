package com.vtence.molecule.routing;

import com.vtence.molecule.Application;
import com.vtence.molecule.Request;
import com.vtence.molecule.Response;
import com.vtence.molecule.middlewares.Router;
import org.junit.Test;

import static com.vtence.molecule.http.HeaderNames.ACCEPT;
import static com.vtence.molecule.lib.predicates.Requests.DELETE;
import static com.vtence.molecule.lib.predicates.Requests.GET;
import static com.vtence.molecule.lib.predicates.Requests.HEAD;
import static com.vtence.molecule.lib.predicates.Requests.OPTIONS;
import static com.vtence.molecule.lib.predicates.Requests.PATCH;
import static com.vtence.molecule.lib.predicates.Requests.POST;
import static com.vtence.molecule.lib.predicates.Requests.PUT;
import static com.vtence.molecule.lib.predicates.Requests.accepting;
import static com.vtence.molecule.routing.StaticRoutingTest.Echo.echo;
import static com.vtence.molecule.testing.ResponseAssert.assertThat;

public class StaticRoutingTest {

    @Test public void
    onVerbAndPath() throws Exception {
        Router router = new Router()
                .route(GET("/get"), echo("get"))
                .route(POST("/post"), echo("post"))
                .route(PUT("/put"), echo("put"))
                .route(DELETE("/delete"), echo("delete"))
                .route(PATCH("/patch"), echo("patch"))
                .route(HEAD("/head"), echo("head"))
                .route(OPTIONS("/options"), echo("options"));

        assertThat(router.handle(Request.get("/get"))).hasBodyText("get");
        assertThat(router.handle(Request.post("/post"))).hasBodyText("post");
        assertThat(router.handle(Request.put("/put"))).hasBodyText("put");
        assertThat(router.handle(Request.delete("/delete"))).hasBodyText("delete");
        assertThat(router.handle(Request.patch("/patch"))).hasBodyText("patch");
        assertThat(router.handle(Request.head("/head"))).hasBodyText("head");
        assertThat(router.handle(Request.options("/options"))).hasBodyText("options");
    }

    @Test public void
    combiningAcceptConditions() throws Exception {
        Router router = new Router()
                .route(GET("/").and(accepting("application/json")), echo("json"))
                .route(GET("/").and(accepting("image/*")), echo("image"))
                .route(GET("/"), echo("html"));

        assertThat(router.handle(Request.get("/").header(ACCEPT, "text/html"))).hasBodyText("html");
        assertThat(router.handle(Request.get("/").header(ACCEPT, "application/json"))).hasBodyText("json");
        assertThat(router.handle(Request.get("/"))).hasBodyText("html");
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
            return Response.ok().done(message);
        }
    }
}