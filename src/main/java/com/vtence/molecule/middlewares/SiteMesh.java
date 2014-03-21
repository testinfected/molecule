package com.vtence.molecule.middlewares;

import com.vtence.molecule.HttpHeaders;
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
import java.io.IOException;
import java.io.Writer;

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
            decorate(response, buffer);
        } else {
            write(response, buffer);
        }
    }

    private void decorate(Response response, BufferedResponse buffer) throws IOException {
        response.removeHeader(HttpHeaders.CONTENT_LENGTH);
        Writer out = new BufferedWriter(response.writer());
        decorator.decorate(out, buffer.body());
        out.flush();
    }

    private void write(Response response, BufferedResponse buffer) throws IOException {
        response.outputStream(buffer.size()).write(buffer.content());
    }

    private boolean shouldDecorate(Response response) {
        return selector.selected(response);
    }
}
