package com.vtence.molecule.middlewares;

import com.vtence.molecule.Body;
import com.vtence.molecule.ChunkedBody;
import com.vtence.molecule.Request;
import com.vtence.molecule.Response;
import com.vtence.molecule.decoration.Decorator;
import com.vtence.molecule.decoration.HtmlDocumentProcessor;
import com.vtence.molecule.decoration.HtmlPageSelector;
import com.vtence.molecule.decoration.Layout;
import com.vtence.molecule.decoration.PageCompositor;
import com.vtence.molecule.decoration.Selector;
import com.vtence.molecule.util.BufferedResponse;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;

import static com.vtence.molecule.HttpHeaders.CONTENT_LENGTH;

public class SiteMesh extends AbstractMiddleware {

    private final Selector selector;
    private final Decorator decorator;

    public static SiteMesh html(Layout layout) {
        return new SiteMesh(new HtmlPageSelector(), new PageCompositor(new HtmlDocumentProcessor(), layout));
    }

    public SiteMesh(Selector selector, Decorator decorator) {
        this.selector = selector;
        this.decorator = decorator;
    }

    public void handle(Request request, Response response) throws Exception {
        BufferedResponse buffer = new BufferedResponse(response);
        forward(request, buffer);
        if (shouldDecorate(buffer)) {
            response.removeHeader(CONTENT_LENGTH);
            response.body(new DecoratedBody(decorator, buffer.body(), buffer.charset()));
        } else {
            response.body(buffer.body());
        }
    }

    private boolean shouldDecorate(Response response) {
        return selector.selected(response);
    }

    private static class DecoratedBody extends ChunkedBody {
        private final Decorator decorator;
        private final Body body;
        private final Charset charset;

        public DecoratedBody(Decorator decorator, Body body, Charset charset) {
            this.decorator = decorator;
            this.charset = charset;
            this.body = body;
        }

        public void writeTo(OutputStream out) throws IOException {
            Writer writer = new BufferedWriter(new OutputStreamWriter(out, charset));
            decorator.decorate(writer, toString(body));
            writer.flush();
        }

        private String toString(Body body) throws IOException {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            body.writeTo(buffer);
            return buffer.toString(charset.name());
        }

        public void close() throws IOException {
            body.close();
        }
    }
}
