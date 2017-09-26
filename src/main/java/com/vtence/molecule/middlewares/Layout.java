package com.vtence.molecule.middlewares;

import com.vtence.molecule.Application;
import com.vtence.molecule.Body;
import com.vtence.molecule.Middleware;
import com.vtence.molecule.Request;
import com.vtence.molecule.Response;
import com.vtence.molecule.decoration.ContentProcessor;
import com.vtence.molecule.decoration.Decorator;
import com.vtence.molecule.decoration.HtmlDocumentProcessor;
import com.vtence.molecule.decoration.HtmlPageSelector;
import com.vtence.molecule.decoration.LayoutTemplate;
import com.vtence.molecule.decoration.Selector;
import com.vtence.molecule.templating.Template;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.concurrent.CompletionException;
import java.util.function.Consumer;

import static com.vtence.molecule.http.HeaderNames.CONTENT_LENGTH;

public class Layout implements Middleware {

    private final Selector selector;
    private final ContentProcessor processor;
    private final Decorator decorator;

    public static Layout html(Template<Map<String, String>> template) {
        return html(new LayoutTemplate(template));
    }

    public static Layout html(Decorator decorator) {
        return new Layout(new HtmlPageSelector(), new HtmlDocumentProcessor(), decorator);
    }

    public Layout(Selector selector, ContentProcessor processor, Decorator decorator) {
        this.selector = selector;
        this.processor = processor;
        this.decorator = decorator;
    }

    public Application then(Application next) {
        return request -> next.handle(request).whenSuccessful(decorate(request));
    }

    private Consumer<Response> decorate(Request request) {
        return response -> {
            if (selectForDecoration(response)) {
                try {
                    applyDecoration(request, response);
                } catch (IOException wontHappen) {
                    throw new CompletionException(wontHappen);
                }
            }
        };
    }

    private boolean selectForDecoration(Response response) {
        return selector.selected(response);
    }

    private void applyDecoration(Request request, Response response) throws IOException {
        response.removeHeader(CONTENT_LENGTH);
        String content = render(response.body(), response.charset());
        Map<String, String> chunks = processor.process(content);
        response.body(decorator.merge(request, chunks));
    }

    private String render(Body body, Charset charset) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        body.writeTo(buffer, charset);
        return buffer.toString(charset.name());
    }
}