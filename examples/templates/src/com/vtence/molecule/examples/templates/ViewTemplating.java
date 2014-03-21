package com.vtence.molecule.examples.templates;

import com.vtence.molecule.Application;
import com.vtence.molecule.Request;
import com.vtence.molecule.Response;
import com.vtence.molecule.mustache.JMustacheRenderer;
import com.vtence.molecule.simple.SimpleServer;
import com.vtence.molecule.templating.Templates;
import com.vtence.molecule.templating.View;

import java.io.File;
import java.io.IOException;

// Access at http://localhost:8080/?name=Vincent
public class ViewTemplating {

    private static final int TEMPLATE_DIR = 0;

    public static void main(String[] args) throws IOException {
        // Specify location of the template folder as a program argument
        File templateDir = new File(args.length != 0 ?
                args[TEMPLATE_DIR] : "examples/templates/views");

        SimpleServer server = new SimpleServer(8080);

        // We use Mustache templates with an .html extension
        Templates templates = new Templates(
                new JMustacheRenderer().templateDir(templateDir).extension("html"));

        // Typically, you would pass the view as a constructor parameter to the controller
        final View hello = templates.html("hello");

        server.run(new Application() {
            public void handle(final Request request, final Response response) throws Exception {
                // Mustache can use any object or a Map as a rendering context
                hello.render(response, new Object() {
                    String name = request.parameter("name") != null ?
                        request.parameter("name") : "World";
                });
            }
        });
    }
}
