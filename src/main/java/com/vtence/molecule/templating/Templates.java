package com.vtence.molecule.templating;

import com.vtence.molecule.Response;
import com.vtence.molecule.util.Charsets;
import com.vtence.molecule.util.MimeTypes;

import java.io.IOException;
import java.nio.charset.Charset;

public class Templates {
    private final RenderingEngine renderer;

    private Charset charset = Charsets.UTF_8;
    private String mediaType = MimeTypes.HTML;

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

    public Templates ofType(String mediaType) {
        this.mediaType = mediaType;
        return this;
    }

    public Template named(final String name) {
        return new Template() {
            public void render(Response response, Object context) throws IOException {
                response.contentType(mediaType + "; charset=" + charset.name().toLowerCase());
                response.body(new TemplateBody(renderer, name, context));
            }
        };
    }
}
