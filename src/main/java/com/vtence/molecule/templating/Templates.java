package com.vtence.molecule.templating;

public class Templates {
    private final RenderingEngine renderer;

    public Templates(RenderingEngine renderer) {
        this.renderer = renderer;
    }

    public <T> Template<T> named(final String name) {
        return context -> new TemplateBody(renderer, name, context);
    }
}