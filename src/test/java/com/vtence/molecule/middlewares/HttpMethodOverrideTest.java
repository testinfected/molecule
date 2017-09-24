package com.vtence.molecule.middlewares;

import com.vtence.molecule.Application;
import com.vtence.molecule.Request;
import com.vtence.molecule.Response;
import org.junit.Test;

import static com.vtence.molecule.testing.ResponseAssert.assertThat;

public class HttpMethodOverrideTest {

    HttpMethodOverride methodOverride = new HttpMethodOverride();

    @Test public void
    doesNotAffectGetMethods() throws Exception {
        Response response = methodOverride.then(echoMethodName())
                                          .handle(Request.get("/")
                                                         .addParameter("_method", "delete"));
        assertThat(response).hasBodyText("GET");
    }

    @Test public void
    leavesMethodUnchangedWhenOverrideParameterAbsent() throws Exception {
        Response response = methodOverride.then(echoMethodName())
                                          .handle(Request.post("/"));
        assertThat(response).hasBodyText("POST");
    }

    @Test public void
    changesPostMethodsAccordingToOverrideParameter() throws Exception {
        Response response = methodOverride.then(echoMethodName())
                                          .handle(Request.post("/")
                                                         .addParameter("_method", "delete"));
        assertThat(response).hasBodyText("DELETE");
    }

    @Test public void
    leavesMethodUnchangedIfMethodIsNotSupported() throws Exception {
        Response response = methodOverride.then(echoMethodName())
                                          .handle(Request.post("/")
                                                         .addParameter("_method", "unsupported"));
        assertThat(response).hasBodyText("POST");
    }

    private Application echoMethodName() {
        return Application.of(request -> Response.ok().done(request.method().name()));
    }
}