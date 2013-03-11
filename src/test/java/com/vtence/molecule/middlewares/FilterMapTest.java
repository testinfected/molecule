package com.vtence.molecule.middlewares;

import com.vtence.molecule.Application;
import com.vtence.molecule.Matcher;
import com.vtence.molecule.Middleware;
import com.vtence.molecule.Request;
import com.vtence.molecule.matchers.Matchers;
import com.vtence.molecule.support.MockRequest;
import com.vtence.molecule.support.MockResponse;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.Sequence;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.vtence.molecule.support.MockRequest.aRequest;
import static com.vtence.molecule.support.MockResponse.aResponse;

@RunWith(JMock.class)
public class FilterMapTest {

    Mockery context = new JUnit4Mockery();
    FilterMap filters = new FilterMap();

    Application successor = context.mock(Application.class, "successor");
    Sequence filtering = context.sequence("filtering");

    MockRequest request = aRequest();
    MockResponse response = aResponse();

    @Before public void
    chainWithSuccessor() {
        filters.connectTo(successor);
    }

    @Test public void
    immediatelyForwardsRequestWhenNoFilterIsRegistered() throws Exception {
        expectForward();
        filters.handle(request, response);
    }

    @Test public void
    runsRequestThroughFilterThatMatches() throws Exception {
        final Middleware ignored = context.mock(Middleware.class, "ignored");
        filters.map(noRequest(), ignored);
        final Middleware applies = context.mock(Middleware.class, "applies");
        filters.map(anyRequest(), applies);

        context.checking(new Expectations() {{
            oneOf(applies).connectTo(successor); inSequence(filtering);
            oneOf(applies).handle(request, response); inSequence(filtering);
        }});

        filters.handle(request, response);
    }

    @Test public void
    forwardsRequestIfNoFilterMatches() throws Exception {
        final Middleware filter = context.mock(Middleware.class);
        filters.map(noRequest(), filter);

        expectForward();
        filters.handle(request, response);
    }

    @Test public void
    runsRequestThroughFilterMappedToPrefixOfRequestPath() throws Exception {
        request.withPath("/filtered/path");

        final Middleware filter = context.mock(Middleware.class);
        filters.map("/filtered", filter);

        context.checking(new Expectations() {{
            allowing(filter).connectTo(successor);
            oneOf(filter).handle(request, response);
        }});

        filters.handle(request, response);
    }

    @Test public void
    appliesLastRegisteredOfMatchingFilters() throws Exception {
        final Middleware ignored = context.mock(Middleware.class, "ignored");
        filters.map(anyRequest(), ignored);
        final Middleware applies = context.mock(Middleware.class, "applies");
        filters.map(anyRequest(), applies);

        context.checking(new Expectations() {{
            allowing(applies).connectTo(successor);
            oneOf(applies).handle(request, response);
        }});

        filters.handle(request, response);
    }

    private void expectForward() throws Exception {
        context.checking(new Expectations() {{
            oneOf(successor).handle(with(same(request)), with(same(response)));
        }});
    }

    private Matcher<Request> anyRequest() {
        return Matchers.anything();
    }

    private Matcher<Request> noRequest() {
        return Matchers.nothing();
    }
}
