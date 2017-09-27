package com.vtence.molecule.templating;

import com.vtence.molecule.Response;
import org.junit.Test;

import java.io.IOException;

import static com.vtence.molecule.testing.ResourceLocator.locateOnClasspath;
import static com.vtence.molecule.testing.ResponseAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

public class TemplatesTest {
    RenderingEngine renderer = new JMustacheRenderer().fromDir(locateOnClasspath("views"));
    Templates templates = new Templates(renderer);

    Template template = templates.named("hello");

    @Test public void
    rendersTemplateUsingProvidedContext() throws IOException {
        Response response = Response.ok()
                                    .body(template.render(new Context()));
        assertThat(response).hasBodyText(containsString("Hello World"));
    }

    public static class Context {
        String name = "World";
    }
}