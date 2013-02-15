package com.vtence.molecule.middlewares;

import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.States;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import com.vtence.molecule.Application;
import com.vtence.molecule.HttpException;
import com.vtence.molecule.Request;
import com.vtence.molecule.Response;
import com.vtence.molecule.support.MockRequest;
import com.vtence.molecule.support.MockResponse;

import javax.sql.DataSource;
import java.sql.Connection;

import static org.jmock.Expectations.same;
import static com.vtence.molecule.support.MockRequest.aRequest;
import static com.vtence.molecule.support.MockResponse.aResponse;

@RunWith(JMock.class)
public class ConnectionScopeTest {

    Mockery context = new JUnit4Mockery();
    DataSource dataSource = context.mock(DataSource.class);
    Application successor = context.mock(Application.class, "successor");
    ConnectionScope connectionScope = new ConnectionScope(dataSource);

    Connection connection = context.mock(Connection.class);
    States connectionStatus = context.states("connection").startsAs("closed");

    MockRequest request = aRequest();
    MockResponse response = aResponse();

    @Before public void
    chainWithSuccessor() throws Exception {
        context.checking(new Expectations() {{
            allowing(dataSource).getConnection(); will(returnValue(connection)); when(connectionStatus.is("closed"));
                then(connectionStatus.is("opened"));
            oneOf(connection).close(); when(connectionStatus.is("opened"));
                then(connectionStatus.is("closed"));
        }});

        connectionScope.connectTo(successor);
    }

    @Test public void
    makesConnectionAvailableToSuccessor() throws Exception {
        context.checking(new Expectations() {{
            oneOf(successor).handle(with(aRequestWithConnectionReference(sameConnection(connection))), with(any(Response.class))); when(connectionStatus.is("opened"));
        }});

        connectionScope.handle(request, response);
    }

    @Test public void
    gracefullyClosesConnectionAndRemovesFromScopeWhenAnErrorOccurs() throws Exception {
        context.checking(new Expectations() {{
            allowing(successor).handle(with(any(Request.class)), with(any(Response.class)));
                will(throwException(new HttpException("error")));
        }});

        try {
            connectionScope.handle(request, response);
        } catch (HttpException expected) {
        }

        request.assertAttribute(Connection.class, Matchers.nullValue());
    }

    private Matcher<Object> sameConnection(final Connection connection) {
        return same((Object) connection);
    }

    private Matcher<Request> aRequestWithConnectionReference(Matcher<Object> connection) {
        return new FeatureMatcher<Request, Object>(connection, "a request with connection scope", "connection scope") {
            protected Object featureValueOf(Request actual) {
                return new ConnectionScope.Reference(request).get();
            }
        };
    }
}