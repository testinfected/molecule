package com.vtence.molecule.middlewares;

import com.vtence.molecule.Application;
import com.vtence.molecule.Request;
import com.vtence.molecule.Response;
import com.vtence.molecule.support.MockRequest;
import com.vtence.molecule.support.MockResponse;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static com.vtence.molecule.support.MockRequest.aRequest;
import static com.vtence.molecule.support.MockResponse.aResponse;

public class StaticAssetsTest {

    @Rule public JUnitRuleMockery context = new JUnitRuleMockery();
    Application fileServer = new Application() {
        public void handle(Request request, Response response) throws Exception {
            response.body(request.pathInfo());
        }
    };
    StaticAssets assets = new StaticAssets(fileServer, "/favicon.ico");

    MockRequest request = aRequest();
    MockResponse response = aResponse();

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
        assets.handle(request.withPath("/favicon.ico"), response);
        response.assertBody("/favicon.ico");
    }

    @Test public void
    servesFileWhenPathMatchesUrlPrefix() throws Exception {
        assets.serve("/assets");
        assets.handle(request.withPath("/assets/images/logo.png"), response);
        response.assertBody("/assets/images/logo.png");
    }

    @Test public void
    servesIndexFileIfPathIndicatesADirectory() throws Exception {
        assets.serve("/faq").index("index.html");
        assets.handle(request.withPath("/faq/"), response);
        response.assertBody("/faq/index.html");
    }

    @Test public void
    forwardsWhenPathIsNotMatched() throws Exception {
        assets.handle(request.withPath("/"), response);
        response.assertBody("Forwarded");
    }
}