package com.vtence.molecule.templating;

import com.vtence.molecule.Response;
import org.junit.Test;

import java.io.IOException;

import static com.vtence.molecule.testing.ResponseAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

public class TemplatesTest {
    RenderingEngine renderer = JMustacheRenderer.fromClasspath("views");

    Templates templates = new Templates(renderer);

    Template<Context> template = templates.named("hello");

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