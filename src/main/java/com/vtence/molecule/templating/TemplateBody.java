package com.vtence.molecule.templating;

import com.vtence.molecule.ChunkedBody;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;

public class TemplateBody extends ChunkedBody {
    private final RenderingEngine renderer;
    private final Object context;
    private final Charset charset;
    private final String name;

    public TemplateBody(RenderingEngine renderer, String templateName, Object context, Charset charset) {
        this.renderer = renderer;
        this.context = context;
        this.charset = charset;
        this.name = templateName;
    }

    public void writeTo(OutputStream out) throws IOException {
        Writer writer = new BufferedWriter(new OutputStreamWriter(out, charset));
        renderer.render(writer, name, context);
        writer.flush();
    }

    public void close() throws IOException {
    }
}
