package com.vtence.molecule.middlewares;

import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import com.vtence.molecule.Application;
import com.vtence.molecule.HttpMethod;
import com.vtence.molecule.Request;
import com.vtence.molecule.Response;
import com.vtence.molecule.support.MockRequest;
import com.vtence.molecule.support.MockResponse;

import static com.vtence.molecule.HttpMethod.*;
import static com.vtence.molecule.support.MockRequest.aRequest;
import static com.vtence.molecule.support.MockResponse.aResponse;

@RunWith(JMock.class)
public class HttpMethodOverrideTest {
    Mockery context = new JUnit4Mockery();
    HttpMethodOverride methodOverride = new HttpMethodOverride();

    Application successor = context.mock(Application.class, "successor");

    MockRequest request = aRequest();
    MockResponse response = aResponse();

    @Before public void
    chainWithSuccessor()  {
        methodOverride.connectTo(successor);
    }

    @Test public void
    doesNotAffectGetMethods() throws Exception {
        request.addParameter("_method", "delete");

        context.checking(new Expectations() {{
            oneOf(successor).handle(with(aRequestWithMethod(GET)), with(any(Response.class)));
        }});

        methodOverride.handle(request.withMethod(GET), response);
    }

    @Test public void
    doesNotAffectPostMethodsWhenOverrideParameterIsNotSet() throws Exception {

        context.checking(new Expectations() {{
            oneOf(successor).handle(with(aRequestWithMethod(POST)), with(any(Response.class)));
        }});

        methodOverride.handle(request.withMethod(POST), response);
    }

    @Test public void
    changesPostMethodsAccordingToOverrideParameter() throws Exception {
        request.addParameter("_method", "delete");

        context.checking(new Expectations() {{
            oneOf(successor).handle(with(aRequestWithMethod(DELETE)), with(any(Response.class)));
        }});

        methodOverride.handle(request.withMethod(POST), response);
    }

    @Test public void
    doesNotChangeMethodIfOverriddenMethodIsNotSupported() throws Exception {
        request.withParameter("_method", "foo");

        context.checking(new Expectations() {{
            oneOf(successor).handle(with(aRequestWithMethod(POST)), with(any(Response.class)));
        }});

        methodOverride.handle(request.withMethod(POST), response);
    }

    private Matcher<Request> aRequestWithMethod(HttpMethod method) {
        return new FeatureMatcher<Request, HttpMethod>(Matchers.equalTo(method), "a request with method", "method") {
            protected HttpMethod featureValueOf(Request request) {
                return request.method();
            }
        };
    }
}