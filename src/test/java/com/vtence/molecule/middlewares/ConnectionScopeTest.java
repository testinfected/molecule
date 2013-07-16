package com.vtence.molecule.middlewares;

import com.vtence.molecule.Application;
import com.vtence.molecule.HttpException;
import com.vtence.molecule.Request;
import com.vtence.molecule.Response;
import com.vtence.molecule.support.MockRequest;
import com.vtence.molecule.support.MockResponse;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.States;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.sql.DataSource;
import java.sql.Connection;

import static com.vtence.molecule.support.MockRequest.aRequest;
import static com.vtence.molecule.support.MockResponse.aResponse;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.fail;

@RunWith(JMock.class)
public class ConnectionScopeTest {

    Mockery context = new JUnit4Mockery();
    DataSource dataSource = context.mock(DataSource.class);
    ConnectionScope connectionScope = new ConnectionScope(dataSource);

    Connection connection = context.mock(Connection.class);
    States connectionStatus = context.states("connection").startsAs("closed");

    MockRequest request = aRequest();
    MockResponse response = aResponse();

    @Before public void
    expectConnectionToBeReleased() throws Exception {
        context.checking(new Expectations() {{
            allowing(dataSource).getConnection(); will(returnValue(connection)); when(connectionStatus.is("closed"));
                then(connectionStatus.is("opened"));
            oneOf(connection).close(); when(connectionStatus.is("opened")); then(connectionStatus.is("closed"));
        }});
    }

    @Test public void
    opensConnectionAndMakesAvailableAsRequestAttribute() throws Exception {
        connectionScope.connectTo(reportPresenceOfAttribute(Connection.class, connection));
        connectionScope.handle(request, response);
        assertScoping("on");
    }

    @Test public void
    gracefullyClosesConnectionAndRemovesFromScopeWhenAnErrorOccurs() throws Exception {
        connectionScope.connectTo(crashWith(new HttpException("Boom!")));

        try {
            connectionScope.handle(request, response);
            fail("HttpException did not bubble up");
        } catch (HttpException expected) {
        }

        request.assertAttribute(Connection.class, nullValue());
    }

    private Application crashWith(final HttpException error) {
        return new Application() {
            public void handle(Request request, Response response) throws Exception {
                throw error;
            }
        };
    }

    private Application reportPresenceOfAttribute(final Object key, final Object value) {
        return new Application() {
            public void handle(Request request, Response response) throws Exception {
                response.body(request.attribute(key) == value ? "on" : "off");
            }
        };
    }

    private void assertScoping(String state) {
        assertThat("scoping", response.body(), equalTo(state));
    }
}