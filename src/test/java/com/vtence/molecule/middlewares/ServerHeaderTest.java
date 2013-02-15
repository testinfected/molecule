package com.vtence.molecule.middlewares;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import com.vtence.molecule.Application;
import com.vtence.molecule.support.MockRequest;
import com.vtence.molecule.support.MockResponse;

import static com.vtence.molecule.support.MockRequest.aRequest;
import static com.vtence.molecule.support.MockResponse.aResponse;

@RunWith(JMock.class)
public class ServerHeaderTest {

    Mockery context = new JUnit4Mockery();
    Application successor = context.mock(Application.class, "successor");

    String serverName = "server/version";
    ServerHeader serverHeader = new ServerHeader(serverName);

    MockRequest request = aRequest();
    MockResponse response = aResponse();

    @Before public void
    chainWithSuccessor() {
        serverHeader.connectTo(successor);
    }

    @Test public void
    setsServerHeaderAndForwardsRequest() throws Exception {
        context.checking(new Expectations() {{
            oneOf(successor).handle(with(request), with(response));
        }});

        serverHeader.handle(request, response);
        response.assertHeader("Server", serverName);
    }
}