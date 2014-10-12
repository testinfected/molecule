package examples.templating;

import com.vtence.molecule.Application;
import com.vtence.molecule.Request;
import com.vtence.molecule.Response;
import com.vtence.molecule.WebServer;
import com.vtence.molecule.middlewares.Layout;
import com.vtence.molecule.routing.DynamicRoutes;
import com.vtence.molecule.support.ResourceLocator;
import com.vtence.molecule.templating.JMustacheRenderer;
import com.vtence.molecule.templating.Template;
import com.vtence.molecule.templating.Templates;

import java.io.File;
import java.io.IOException;

public class TemplatingAndLayoutExample {

    public void run(WebServer server) throws IOException {
        File templateDir = ResourceLocator.locateOnClasspath("examples/templates");
        // We use Mustache templates with an .html extension
        Templates templates = new Templates(new JMustacheRenderer().fromDir(templateDir).extension("html"));
        final Template layout = templates.named("layout");
        final Template hello = templates.named("greeting");

        // Apply a common layout to all rendered pages
        server.filter("/", Layout.html(layout))
              .start(new DynamicRoutes() {{
                  get("/hello").to(new Application() {
                      public void handle(final Request request, final Response response) throws Exception {
                          response.contentType("text/html; charset=utf-8");
                          // Mustache can use any object or a Map as a rendering context
                          response.body(hello.render(new Object() {
                              String name = request.parameter("name") != null ? request.parameter("name") : "World";
                          }));
                      }
                  });
              }});
    }

    public static void main(String[] args) throws IOException {
        TemplatingAndLayoutExample example = new TemplatingAndLayoutExample();
        // Run the default web server
        WebServer webServer = WebServer.create();
        example.run(webServer);
        System.out.println("Access at " + webServer.uri() + "/hello?name=Gandalf");
    }
}
