package examples.layout;

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

public class LayoutExample {

    public void run(WebServer server) throws IOException {
        File templateDir = ResourceLocator.locateOnClasspath("examples/layout/content");
        // We use Mustache templates with an .html extension
        final Templates templates = new Templates(new JMustacheRenderer().fromDir(templateDir).extension("html"));
        final Template layout = templates.named("layout");
        final Template hello = templates.named("hello");

        server.filter("/", Layout.html(layout))
              .start(new DynamicRoutes() {{
                  get("/hello").to(new Application() {
                      public void handle(Request request, Response response) throws Exception {
                          response.contentType("text/html; charset=utf-8");
                          response.body(hello.render(null));
                      }
                  });
              }});
    }

    public static void main(String[] args) throws IOException {
        WebServer webServer = WebServer.create();
        LayoutExample example = new LayoutExample();
        example.run(webServer);
        System.out.println("Access at " + webServer.uri() + "/hello");
    }
}
