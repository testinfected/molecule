package examples.templating;

import com.vtence.molecule.Application;
import com.vtence.molecule.Request;
import com.vtence.molecule.Response;
import com.vtence.molecule.WebServer;
import com.vtence.molecule.middlewares.Layout;
import com.vtence.molecule.routing.DynamicRoutes;
import com.vtence.molecule.templating.JMustacheRenderer;
import com.vtence.molecule.templating.Template;
import com.vtence.molecule.templating.Templates;

import java.io.IOException;

import static com.vtence.molecule.testing.ResourceLocator.locateOnClasspath;

public class TemplatingAndLayoutExample {

    public void run(WebServer server) throws IOException {
        // We use Mustache templates with an .html extension
        Templates templates = new Templates(
                new JMustacheRenderer().fromDir(locateOnClasspath("examples/templates")).extension("html"));
        final Template layout = templates.named("layout");
        final Template greeting = templates.named("greeting");

        // Apply a common layout to all rendered pages
        server.filter("/", Layout.html(layout))
              .start(new DynamicRoutes() {{
                  get("/hello").to(new Application() {
                      public void handle(final Request request, final Response response) throws Exception {
                          response.contentType("text/html; charset=utf-8");
                          String name = request.parameter("name") != null ? request.parameter("name") : "World";
                          // Mustache can use any object or a Map as a rendering context
                          response.body(greeting.render(new User(name)));
                      }
                  });
              }});
    }

    private static class User {
        public final String name;

        public User(String name) {
            this.name = name;
        }
    }

    public static void main(String[] args) throws IOException {
        TemplatingAndLayoutExample example = new TemplatingAndLayoutExample();
        // Run the default web server
        WebServer webServer = WebServer.create();
        example.run(webServer);
        System.out.println("Access at " + webServer.uri() + "/hello?name=Gandalf");
    }
}