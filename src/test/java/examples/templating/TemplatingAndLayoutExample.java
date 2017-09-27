package examples.templating;

import com.vtence.molecule.Response;
import com.vtence.molecule.WebServer;
import com.vtence.molecule.middlewares.Layout;
import com.vtence.molecule.routing.DynamicRoutes;
import com.vtence.molecule.templating.JMustacheRenderer;
import com.vtence.molecule.templating.Template;
import com.vtence.molecule.templating.Templates;

import java.io.IOException;
import java.util.Map;

import static com.vtence.molecule.testing.ResourceLocator.locateOnClasspath;

/**
 * <p>
 * In this example we demonstrate rendering output using a template engine. We have a template for a common site
 * layout and templates for the individual pages.
 * </p>
 * <p>
 * Our templates are Mustache templates with an <code>.html</code> extension. We use JMustache as the template
 * engine.
 * </p>
 */
public class TemplatingAndLayoutExample {

    public void run(WebServer server) throws IOException {
        // We use JMustache to render Mustache templates with an .html extension
        Templates templates = new Templates(
                new JMustacheRenderer().fromDir(locateOnClasspath("examples/templates")).extension("html"));
        // This is template for the shared application layout
        final Template<Map<String, String>> layout = templates.named("layout");
        // This is the template for the greeting.html page
        final Template<User> greeting = templates.named("greeting");

        // Apply a common site layout to requests under the / path, i.e. to all rendered pages
        server.filter("/", Layout.html(layout))
              .route(new DynamicRoutes() {{
                  get("/hello").to(request -> {
                      Response response = Response.ok()
                                                  .contentType("text/html; charset=utf-8");
                      String name = request.hasParameter("name") ? request.parameter("name") : "World";
                      // Mustache can use any object or a Map as a rendering context
                      return response.done(greeting.render(new User(name)));
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