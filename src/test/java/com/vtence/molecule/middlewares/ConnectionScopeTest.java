package com.vtence.molecule.middlewares;

import com.vtence.molecule.Request;
import com.vtence.molecule.Response;
import org.jmock.Expectations;
import org.jmock.States;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.concurrent.ExecutionException;

import static com.vtence.molecule.testing.RequestAssert.assertThat;
import static com.vtence.molecule.testing.ResponseAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;

public class ConnectionScopeTest {

    @Rule
    public JUnitRuleMockery context = new JUnitRuleMockery();
    DataSource dataSource = context.mock(DataSource.class);
    ConnectionScope connectionScope = new ConnectionScope(dataSource);

    Connection connection = context.mock(Connection.class);
    States connectionStatus = context.states("connection").startsAs("closed");

    Request request = new Request();
    Response response = new Response();

    @Rule
    public ExpectedException error = ExpectedException.none();

    @Before
    public void expectConnectionToBeReleasedAfterwards() throws Exception {
        context.checking(new Expectations() {{
            allowing(dataSource).getConnection();
            will(returnValue(connection));
            when(connectionStatus.is("closed"));
            then(connectionStatus.is("opened"));
            oneOf(connection).close();
            when(connectionStatus.is("opened"));
            then(connectionStatus.is("closed"));
        }});
    }

    @Test
    public void
    opensConnectionAndMakesItAvailableAsRequestAttribute() throws Exception {
        connectionScope.connectTo((request, response) ->
                response.body(request.attribute(Connection.class) == connection ? "opened" : "closed"));
        connectionScope.handle(request, response);
        response.done();

        assertNoExecutionError();
        assertThat(response).hasBodyText("opened");
    }

    @Test
    public void
    removesConnectionFromScopeAfterUse() throws Exception {
        connectionScope.handle(request, response);
        assertThat(request).hasAttribute(Connection.class, notNullValue());

        response.done();

        assertNoExecutionError();
        assertThat(request).hasNoAttribute(Connection.class);
    }

    @Test
    public void
    gracefullyClosesConnectionWhenAnErrorOccurs() throws Exception {
        connectionScope.connectTo((request, response) -> {
            throw new Exception("Boom!");
        });

        error.expectMessage("Boom!");
        try {
            connectionScope.handle(request, response);
        } finally {
            assertThat(request).hasNoAttribute(Connection.class);
        }
    }

    @Test
    public void
    gracefullyClosesConnectionWhenAnErrorOccursLater() throws Throwable {
        connectionScope.handle(request, response);
        assertThat(request).hasAttribute(Connection.class, notNullValue());

        response.done(new Exception("Boom!"));
        assertThat(request).hasNoAttribute(Connection.class);
    }

    private void assertNoExecutionError() throws ExecutionException, InterruptedException {
        response.await();
    }
}