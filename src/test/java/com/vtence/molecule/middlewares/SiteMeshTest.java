package com.vtence.molecule.middlewares;

import com.vtence.molecule.Application;
import com.vtence.molecule.Response;
import com.vtence.molecule.decoration.Decorator;
import com.vtence.molecule.decoration.Selector;
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

import java.io.IOException;
import java.io.Writer;

import static com.vtence.molecule.support.MockRequest.aRequest;
import static com.vtence.molecule.support.MockResponse.aResponse;
import static com.vtence.molecule.support.WriteBody.writeBody;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.nullValue;

@RunWith(JMock.class)
public class SiteMeshTest {
    Mockery context = new JUnit4Mockery();
    Selector selector = context.mock(Selector.class);
    SiteMesh siteMesh = new SiteMesh(selector, new FakeDecorator());
    Application successor = context.mock(Application.class, "successor");

    String originalPage = "<plain page>";
    String decoratedPage = "<decorated page>";
    States page = context.states("page").startsAs("selected");

    MockRequest request = aRequest();
    MockResponse response = aResponse();

    @Before public void
    chainWithSuccessor() throws Exception {
        context.checking(new Expectations() {{
            allowing(selector).select(with(any(Response.class))); will(returnValue(true)); when(page.is("selected"));
            allowing(selector).select(with(any(Response.class))); will(returnValue(false)); when(page.isNot("selected"));
            allowing(successor).handle(with(request), with(any(Response.class))); will(writeBody(originalPage));
        }});

        siteMesh.connectTo(successor);
    }

    @Test public void
    runsContentThroughDecoratorWhenPageIsSelected() throws Exception {
        siteMesh.handle(request, response);

        response.assertBody(decoratedPage);
    }


    @Test public void
    removesContentLengthHeaderWhenPageIsSelected() throws Exception {
        response.header("Content-Length", String.valueOf(140));
        siteMesh.handle(request, response);

        response.assertHeader("Content-Length", nullValue());
    }

    @Test public void
    doesNotDecorateContentWhenPageIsNotSelected() throws Exception {
        page.become("unselected");

        siteMesh.handle(request, response);

        response.assertBody(originalPage);
    }

    @Test public void
    preservesOriginalPageEncodingWhenDecorating() throws Exception {
        response.withContentType("text/html; charset=UTF-16");
        decoratedPage = "<The following characters require encoding: éçë>";

        siteMesh.handle(request, response);

        response.assertContentType(containsString("UTF-16"));
        response.assertContentEncodedAs("UTF-16");
    }

    private class FakeDecorator implements Decorator {
        public void decorate(Writer out, String content) throws IOException {
            out.write(decoratedPage);
        }
    }
}
