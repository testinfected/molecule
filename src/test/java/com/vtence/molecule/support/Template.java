package com.vtence.molecule.support;

import com.vtence.molecule.templating.RenderingEngine;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

public class Template {

    private final String template;
    private Object context = new Object();

    public static Template render(String template) {
        return new Template(template);
    }

    public Template(String template) {
        this.template = template;
    }

    public Template with(Object context) {
        this.context = context;
        return this;
    }

    public String asString(RenderingEngine renderer) throws IOException {
        StringWriter buffer = new StringWriter();
        render(renderer, buffer);
        return buffer.toString();
    }

    private void render(RenderingEngine renderer, Writer writer) throws IOException {
        renderer.render(writer, template, context);
    }
}
