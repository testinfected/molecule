package com.vtence.molecule.middlewares;

import com.vtence.molecule.Application;
import com.vtence.molecule.Request;
import com.vtence.molecule.Response;
import com.vtence.molecule.support.MockRequest;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static com.vtence.molecule.support.ResponseAssertions.assertThat;

public class StaticAssetsTest {

    @Rule public JUnitRuleMockery context = new JUnitRuleMockery();
    Application fileServer = new Application() {
        public void handle(Request request, Response response) throws Exception {
            response.body(request.path());
        }
    };
    StaticAssets assets = new StaticAssets(fileServer, "/favicon.ico");

    MockRequest request = new MockRequest();
    Response response = new Response();

    @Before public void
    setUpResponseChain() {
        assets.connectTo(new Application() {
            public void handle(Request request, Response response) throws Exception {
                response.body("Forwarded");
            }
        });
    }

    @Test public void
    servesFileWhenPathMatchesExactly() throws Exception {
        assets.handle(request.path("/favicon.ico"), response);
        assertThat(response).hasBodyText("/favicon.ico");
    }

    @Test public void
    servesFileWhenPathMatchesUrlPrefix() throws Exception {
        assets.serve("/assets");
        assets.handle(request.path("/assets/images/logo.png"), response);
        assertThat(response).hasBodyText("/assets/images/logo.png");
    }

    @Test public void
    servesIndexFileIfPathIndicatesADirectory() throws Exception {
        assets.serve("/faq").index("index.html");
        assets.handle(request.path("/faq/"), response);
        assertThat(response).hasBodyText("/faq/index.html");
    }

    @Test public void
    forwardsWhenPathIsNotMatched() throws Exception {
        assets.handle(request.path("/"), response);
        assertThat(response).hasBodyText("Forwarded");
    }
}