package examples.templating;

import com.vtence.molecule.Application;
import com.vtence.molecule.Request;
import com.vtence.molecule.Response;
import com.vtence.molecule.WebServer;
import com.vtence.molecule.support.ResourceLocator;
import com.vtence.molecule.templating.JMustacheRenderer;
import com.vtence.molecule.templating.Template;
import com.vtence.molecule.templating.Templates;

import java.io.File;
import java.io.IOException;

public class TemplatingExample {

    public void run(WebServer server) throws IOException {
        File templateDir = ResourceLocator.locateOnClasspath("examples/templating/content");
        // We use Mustache templates with an .html extension
        Templates templates = new Templates(new JMustacheRenderer().fromDir(templateDir).extension("html"));

        final Template hello = templates.named("hello");
        server.start(new Application() {
            public void handle(final Request request, final Response response) throws Exception {
                // Mustache can use any object or a Map as a rendering context
                hello.render(response, new Object() {
                    String name = request.parameter("name") != null ? request.parameter("name") : "World";
                });
            }
        });
    }

    public static void main(String[] args) throws IOException {
        // Run server on a random available port
        WebServer webServer = WebServer.create();
        new TemplatingExample().run(webServer);
        System.out.println("Access at " + webServer.uri() + "?name=Gandalf");
    }
}
