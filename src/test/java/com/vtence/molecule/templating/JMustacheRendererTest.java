package com.vtence.molecule.templating;

import org.junit.Test;

import java.io.IOException;

import static com.vtence.molecule.support.TemplateRenderer.render;
import static com.vtence.molecule.templating.JMustacheRenderer.ClasspathTemplateLoader.classpath;
import static com.vtence.molecule.testing.ResourceLocator.locateOnClasspath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

public class JMustacheRendererTest {

    @Test public void
    rendersFromTemplatesFolder() throws IOException {
        JMustacheRenderer mustache = JMustacheRenderer.fromDir(locateOnClasspath("views"));

        String view = render("hello").with(new Object() { String name = "World"; })
                .asString(mustache);
        assertThat("view", view, containsString("Hello World"));
    }

    @Test public void
    rendersFromClasspath() throws IOException {
        JMustacheRenderer mustache = JMustacheRenderer.fromClasspath("views");

        String view = render("hello").with(new Object() { String name = "World"; })
                .asString(mustache);
        assertThat("view", view, containsString("Hello World"));
    }

    @Test public void
    makesTemplateExtensionConfigurable() throws IOException {
        JMustacheRenderer mustache = JMustacheRenderer.from(classpath("views").usingExtension("html"));

        String view = render("cheers").asString(mustache);
        assertThat("view", view, containsString("#{"));
    }

    @Test public void
    makesNullValueConfigurable() throws IOException {
        JMustacheRenderer mustache = JMustacheRenderer.fromClasspath().nullValue("World");

        String view = render("views/hello").with(new Object() { String name = null; }).asString(mustache);
        assertThat("view", view, containsString("Hello World"));
    }

    @Test public void
    makesDefaultValueConfigurable() throws IOException {
        JMustacheRenderer mustache = JMustacheRenderer.fromClasspath("views").defaultValue("World");

        String view = render("hello").with(new Object()).asString(mustache);
        assertThat("view", view, containsString("Hello World"));
    }

    @Test public void
    assumesUtf8EncodingByDefault() throws IOException {
        JMustacheRenderer mustache = JMustacheRenderer.fromClasspath("views");

        String view = render("utf-8").asString(mustache);
        assertThat("view", view, containsString("\u00E6githales"));
    }

    @Test public void
    makesTemplateEncodingConfigurable() throws IOException {
        JMustacheRenderer mustache = JMustacheRenderer.from(classpath("views").usingEncoding("utf-16be"));
        String view = render("utf-16be").asString(mustache);
        assertThat("view", view, containsString("\u00E6githales"));
    }

    @Test public void
    supportsPartialTemplates() throws IOException {
        JMustacheRenderer mustache = JMustacheRenderer.fromClasspath("views");

        String view = render("full").with(new Object() { String name = "World"; })
                .asString(mustache);
        assertThat("view", view, containsString("Hello World"));
    }
}