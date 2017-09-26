package com.vtence.molecule.middlewares;

import com.vtence.molecule.Application;
import com.vtence.molecule.Request;
import com.vtence.molecule.Response;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Rule;
import org.junit.Test;

import static com.vtence.molecule.testing.ResponseAssert.assertThat;

public class StaticAssetsTest {

    @Rule public JUnitRuleMockery context = new JUnitRuleMockery();

    Application fileServer = Application.of(request -> Response.ok().done(request.path()));
    StaticAssets assets = new StaticAssets(fileServer, "/favicon.ico");

    @Test public void
    servesFileWhenPathMatchesExactly() throws Exception {
        Response response = assets.then(forward())
                                  .handle(Request.get("/favicon.ico"));

        assertThat(response).hasBodyText("/favicon.ico");
    }

    @Test public void
    servesFileWhenPathMatchesUrlPrefix() throws Exception {
        assets.serve("/assets");
        Response response = assets.then(forward())
                                  .handle(Request.get("/assets/images/logo.png"));

        assertThat(response).hasBodyText("/assets/images/logo.png");
    }

    @Test public void
    servesIndexFileIfPathIndicatesADirectory() throws Exception {
        assets.serve("/faq").index("index.html");
        Response response = assets.then(forward())
                                  .handle(Request.get("/faq/"));

        assertThat(response).hasBodyText("/faq/index.html");
    }

    @Test public void
    forwardsWhenPathIsNotMatched() throws Exception {
        Response response = assets.then(forward())
                                  .handle(Request.get("/"));

        assertThat(response).hasBodyText("Forwarded");
    }

    private Application forward() {
        return request -> Response.ok().done("Forwarded");
    }
}