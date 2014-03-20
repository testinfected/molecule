package com.vtence.molecule.templating;

import com.vtence.molecule.util.Charsets;

import java.nio.charset.Charset;

public class Templates {
    private final RenderingEngine renderer;
    private Charset charset = Charsets.UTF_8;

    public static Templates renderedWith(RenderingEngine engine) {
        return new Templates(engine);
    }

    public Templates(RenderingEngine renderer) {
        this.renderer = renderer;
    }

    public Templates as(String charsetName) {
        return as(Charset.forName(charsetName));
    }

    private Templates as(Charset charset) {
        this.charset = charset;
        return this;
    }

    public ViewTemplate html(String name) {
        return new ViewTemplate(renderer, name, "text/html; charset=" + charset.name().toLowerCase());
    }
}
