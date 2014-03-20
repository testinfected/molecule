package com.vtence.molecule.templating;

import com.vtence.molecule.mustache.JMustacheRenderer;
import com.vtence.molecule.support.MockResponse;
import com.vtence.molecule.support.ResourceLocator;
import org.junit.Test;

import java.io.IOException;

import static com.vtence.molecule.support.MockResponse.aResponse;
import static org.hamcrest.Matchers.containsString;

public class HtmlTemplateTest {
    Templates templates = Templates.renderedWith(
            JMustacheRenderer.lookIn(ResourceLocator.locateOnClasspath("views")));

    ViewTemplate template = templates.html("hello");
    MockResponse response = aResponse();

    @Test public void
    rendersUsingProvidedContext() throws IOException {
        template.render(response, new Context());
        response.assertBody(containsString("Hello World"));
    }

    @Test public void
    setsMediaTypeUsingUtf8EncodingByDefault() throws IOException {
        template.render(response, new Context());
        response.assertContentType("text/html; charset=utf-8");
    }

    public static class Context {
        String name = "World";
    }
}
