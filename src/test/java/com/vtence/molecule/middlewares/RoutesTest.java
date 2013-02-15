package com.vtence.molecule.middlewares;

import com.vtence.molecule.Application;
import com.vtence.molecule.Matcher;
import com.vtence.molecule.Request;
import com.vtence.molecule.Response;
import com.vtence.molecule.matchers.Nothing;
import com.vtence.molecule.routing.Route;
import com.vtence.molecule.support.MockRequest;
import com.vtence.molecule.support.MockResponse;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.vtence.molecule.matchers.Matchers.anyRequest;
import static com.vtence.molecule.support.MockRequest.aRequest;
import static com.vtence.molecule.support.MockResponse.aResponse;

@RunWith(JMock.class)
public class RoutesTest {

    Routes routes = new Routes(new NotFound());

    MockRequest request = aRequest();
    MockResponse response = aResponse();

    Mockery context = new JUnit4Mockery();
    Application wrongApp = context.mock(Application.class, "wrong app");
    Application preferredApp = context.mock(Application.class, "preferred app");
    Application alternateApp = context.mock(Application.class, "alternate app");
    Application fallbackApp = context.mock(Application.class, "fallback app");

    @Test public void
    routesToDefaultApplicationWhenNoRouteMatches() throws Exception {
        routes.defaultsTo(fallbackApp).add(new StaticRoute(noRequest(), wrongApp));

        context.checking(new Expectations() {{
            never(wrongApp);
            oneOf(fallbackApp).handle(with(same(request)), with(same(response)));
        }});

        routes.handle(request, response);
    }

    @Test public void
    dispatchesToFirstRouteThatMatches() throws Exception {
        context.checking(new Expectations() {{
            oneOf(preferredApp).handle(with(same(request)), with(same(response)));
            never(alternateApp);
        }});
        routes.add(new StaticRoute(anyRequest(), preferredApp));
        routes.add(new StaticRoute(anyRequest(), alternateApp));

        routes.handle(request, response);
    }

    private Matcher<Request> noRequest() {
        return new Nothing<Request>();
    }

    private class StaticRoute implements Route {
        private final Matcher<Request> requestMatcher;
        private final Application app;

        public StaticRoute(Matcher<Request> requestMatcher, Application app) {
            this.requestMatcher = requestMatcher;
            this.app = app;
        }

        public void handle(Request request, Response response) throws Exception {
            app.handle(request, response);
        }

        public boolean matches(Request actual) {
            return requestMatcher.matches(actual);
        }
    }
}
