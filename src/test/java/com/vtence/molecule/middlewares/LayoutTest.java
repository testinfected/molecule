package com.vtence.molecule.middlewares;

import com.vtence.molecule.Application;
import com.vtence.molecule.Body;
import com.vtence.molecule.Request;
import com.vtence.molecule.Response;
import com.vtence.molecule.lib.TextBody;
import com.vtence.molecule.decoration.ContentProcessor;
import com.vtence.molecule.decoration.Decorator;
import com.vtence.molecule.decoration.Selector;
import com.vtence.molecule.support.MockRequest;

import static com.vtence.molecule.helpers.Charsets.UTF_8;
import static com.vtence.molecule.support.ResponseAssertions.assertThat;
import org.jmock.Expectations;
import org.jmock.States;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.containsString;

public class LayoutTest {
    @Rule public JUnitRuleMockery context = new JUnitRuleMockery();
    Selector selector = context.mock(Selector.class);
    Layout layout = new Layout(selector, new StubProcessor(), new StubDecorator());

    States page = context.states("page").startsAs("selected");

    MockRequest request = new MockRequest();
    Response response = new Response();

    @Before public void
    selectPage() throws Exception {
        context.checking(new Expectations() {{
            allowing(selector).selected(with(any(Response.class))); will(returnValue(true)); when(page.is("selected"));
            allowing(selector).selected(with(any(Response.class))); will(returnValue(false)); when(page.isNot("selected"));
        }});
    }

    @Test public void
    runsContentThroughDecoratorWhenPageIsSelected() throws Exception {
        layout.connectTo(new Application() {
            public void handle(Request request, Response response) throws Exception {
                response.body("raw content");
            }
        });

        layout.handle(request, response);
        assertThat(response).hasBodyText("<decorated>raw content</decorated>");
    }

    @Test public void
    removesContentLengthHeaderIfDecorating() throws Exception {
        response.header("Content-Length", 140);
        layout.handle(request, response);
        assertThat(response).hasNoHeader("Content-Length");
    }

    @Test public void
    leavesContentUntouchedIfNoDecorationOccurs() throws Exception {
        layout.connectTo(new Application() {
            public void handle(Request request, Response response) throws Exception {
                response.body("original content");
            }
        });
        page.become("unselected");
        layout.handle(request, response);
        assertThat(response).hasBodyText("original content");
    }

    @Test public void
    preservesOriginalResponseEncodingWhenDecorating() throws Exception {
        layout.connectTo(new Application() {
            public void handle(Request request, Response response) throws Exception {
                response.body("encoded content (éçëœ)");
            }
        });

        response.contentType("text/html; charset=utf-8");
        layout.handle(request, response);

        assertThat(response).hasContentType("text/html; charset=utf-8")
                            .hasBodyEncoding(UTF_8)
                            .hasBodyText(containsString("éçëœ"));
    }

    private class StubProcessor implements ContentProcessor {
        public Map<String, String> process(String content) {
            Map<String, String> data = new HashMap<String, String>();
            data.put("content", content);
            return data;
        }
    }

    private class StubDecorator implements Decorator {
        public Body merge(Request request, Map<String, String> content) {
            return TextBody.text("<decorated>" + content.get("content") + "</decorated>");
        }
    }
}