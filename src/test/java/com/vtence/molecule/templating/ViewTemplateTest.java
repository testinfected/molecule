package com.vtence.molecule.templating;

import com.vtence.molecule.mustache.JMustacheRenderer;
import com.vtence.molecule.support.MockResponse;
import com.vtence.molecule.support.ResourceLocator;
import com.vtence.molecule.util.MimeTypes;
import org.junit.Test;

import java.io.IOException;

import static com.vtence.molecule.support.MockResponse.aResponse;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.startsWith;

public class ViewTemplateTest {
    Templates templates = Templates.renderedWith(
            JMustacheRenderer.lookIn(ResourceLocator.locateOnClasspath("views")));

    ViewTemplate template = templates.html("hello");
    MockResponse response = aResponse().withContentType("text/plain; charset=utf-8");

    @Test public void
    rendersTemplateUsingProvidedContext() throws IOException {
        template.render(response, new Context());
        response.assertBody(containsString("Hello World"));
    }

    @Test public void
    usesTemplateMediaTypeAsContentType() throws IOException {
        template.render(response, new Context());
        response.assertContentType(startsWith(MimeTypes.HTML));
    }

    @Test public void
    preservesExistingResponseEncoding() throws IOException {
        template.render(response, new Context());
        response.assertContentType(endsWith("charset=utf-8"));
    }

    public static class Context {
        String name = "World";
    }
}
