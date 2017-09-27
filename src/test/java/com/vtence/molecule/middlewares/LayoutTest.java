package com.vtence.molecule.middlewares;

import com.vtence.molecule.Body;
import com.vtence.molecule.Request;
import com.vtence.molecule.Response;
import com.vtence.molecule.decoration.ContentProcessor;
import com.vtence.molecule.decoration.Decorator;
import com.vtence.molecule.decoration.Selector;
import com.vtence.molecule.lib.TextBody;
import org.jmock.Expectations;
import org.jmock.States;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static com.vtence.molecule.testing.ResponseAssert.assertThat;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.CoreMatchers.containsString;

public class LayoutTest {
    @Rule
    public
    JUnitRuleMockery context = new JUnitRuleMockery();
    Selector selector = context.mock(Selector.class);
    Layout layout = new Layout(selector, new StubProcessor(), new StubDecorator());

    States page = context.states("page").startsAs("selected");

    @Before
    public void
    selectPage() throws Exception {
        context.checking(new Expectations() {{
            allowing(selector).selected(with(any(Response.class)));
                will(returnValue(true));
                when(page.is("selected"));
            allowing(selector).selected(with(any(Response.class)));
                will(returnValue(false));
                when(page.isNot("selected"));
        }});
    }

    @Test
    public void
    runsContentThroughDecoratorWhenPageIsSelected() throws Exception {
        Response response = layout.then(request -> Response.ok()
                                                           .done("raw content"))
                                  .handle(Request.get("/"));

        assertNoExecutionError(response);
        assertThat(response).hasBodyText("<decorated>raw content</decorated>");
    }

    @Test
    public void
    removesContentLengthHeaderIfDecorating() throws Exception {
        Response response = layout.then(request -> Response.ok()
                                                           .header("Content-Length", 140)
                                                           .done())
                                  .handle(Request.get("/"));

        assertNoExecutionError(response);
        assertThat(response).hasNoHeader("Content-Length");
    }

    @Test
    public void
    leavesContentUntouchedIfNoDecorationOccurs() throws Exception {
        page.become("unselected");
        Response response = layout.then(request -> Response.ok()
                                                           .done("original content"))
                                  .handle(Request.get("/"));

        assertNoExecutionError(response);
        assertThat(response).hasBodyText("original content");
    }

    @Test
    public void
    preservesOriginalResponseEncodingWhenDecorating() throws Exception {
        Response response = layout.then(request -> Response.ok()
                                                           .contentType("text/html; charset=utf-8")
                                                           .done("encoded content (éçëœ)"))
                                  .handle(Request.get("/"));

        assertNoExecutionError(response);
        assertThat(response).hasContentType("text/html; charset=utf-8")
                            .hasBodyEncoding(UTF_8)
                            .hasBodyText(containsString("éçëœ"));
    }

    private void assertNoExecutionError(Response response) throws ExecutionException, InterruptedException {
        response.await();
    }

    private class StubProcessor implements ContentProcessor {
        public Map<String, String> process(String content) {
            Map<String, String> data = new HashMap<>();
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