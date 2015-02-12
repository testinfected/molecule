package com.vtence.molecule.middlewares;

import com.vtence.molecule.Application;
import com.vtence.molecule.Request;
import com.vtence.molecule.Response;
import org.jmock.Expectations;
import org.jmock.States;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.Connection;

import static com.vtence.molecule.testing.RequestAssert.assertThat;
import static com.vtence.molecule.testing.ResponseAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class ConnectionScopeTest {

    @Rule public JUnitRuleMockery context = new JUnitRuleMockery();
    DataSource dataSource = context.mock(DataSource.class);
    ConnectionScope connectionScope = new ConnectionScope(dataSource);

    Connection connection = context.mock(Connection.class);
    States connectionStatus = context.states("connection").startsAs("closed");

    Request request = new Request();
    Response response = new Response();

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
        connectionScope.connectTo(crashWith(new Exception("Boom!")));

        try {
            connectionScope.handle(request, response);
            fail("HttpException did not bubble up");
        } catch (Exception expected) {
            assertThat("message", expected.getMessage(), equalTo("Boom!"));
        }

        assertThat(request).hasAttribute(Connection.class, nullValue());
    }

    private Application crashWith(final Exception error) {
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
        assertThat(response).hasBodyText(state);
    }
}