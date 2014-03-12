package com.vtence.molecule.templating;

import com.vtence.molecule.util.MimeTypes;

public class Templates {
    private final RenderingEngine renderer;

    public static Templates renderedWith(RenderingEngine engine) {
        return new Templates(engine);
    }

    public Templates(RenderingEngine renderer) {
        this.renderer = renderer;
    }

    public ViewTemplate html(String name) {
        return new ViewTemplate(renderer, name, MimeTypes.HTML);
    }
}
