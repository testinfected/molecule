package com.vtence.molecule.templating;

import com.vtence.molecule.support.MockResponse;
import com.vtence.molecule.util.MimeTypes;
import org.junit.Test;

import java.io.IOException;

import static com.vtence.molecule.support.ResourceLocator.locateOnClasspath;
import static org.hamcrest.Matchers.containsString;

public class TemplatesTest {
    RenderingEngine renderer = new JMustacheRenderer().fromDir(locateOnClasspath("views"));
    Templates templates = new Templates(renderer).ofType(MimeTypes.HTML);

    Template template = templates.named("hello");
    MockResponse response = new MockResponse();

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
