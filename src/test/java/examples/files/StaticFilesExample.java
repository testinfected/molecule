package examples.files;

import com.vtence.molecule.WebServer;
import com.vtence.molecule.middlewares.ApacheCommonLogger;
import com.vtence.molecule.middlewares.FileServer;
import com.vtence.molecule.middlewares.StaticAssets;

import java.io.IOException;
import java.util.logging.Logger;

import static com.vtence.molecule.support.ResourceLocator.locateOnClasspath;

public class StaticFilesExample {

    private final Logger logger;

    public StaticFilesExample(Logger logger) {
        this.logger = logger;
    }

    public void run(WebServer server) throws IOException {
        // Serve files in this directory, based on the path of the request.
        FileServer files = new FileServer(locateOnClasspath("examples/files/content"));
        // Serve static assets (e.g. js files, images, css files, etc) for any request whose path starts
        // with one of the url prefixes specified (in this example, we're serving all requests).
        StaticAssets assets = new StaticAssets(files).serve("/");
        // If request targets a directory (i.e. request path ends with "/"),
        // serve the index.html file located in that directory (the default behavior).
        assets.index("index.html");

        // Log all accesses to the server in apache common log format
        server.add(new ApacheCommonLogger(logger))
              .start(assets);
    }

    public static void main(String[] args) throws IOException {
        // Let's log server access to the console, so we can see content we're serving
        StaticFilesExample example = new StaticFilesExample(Logging.toConsole());
        WebServer webServer = WebServer.create();
        example.run(webServer);
        System.out.println("Access at " + webServer.uri());
    }
}