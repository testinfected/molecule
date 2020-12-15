package examples.files;

import com.vtence.molecule.Response;
import com.vtence.molecule.WebServer;
import com.vtence.molecule.middlewares.ApacheCommonLogger;
import com.vtence.molecule.middlewares.FileServer;
import com.vtence.molecule.middlewares.StaticAssets;

import java.io.IOException;
import java.time.Clock;
import java.util.Locale;
import java.util.logging.Logger;

import static com.vtence.molecule.http.HttpStatus.NOT_FOUND;
import static com.vtence.molecule.testing.ResourceLocator.locateOnClasspath;

/**
 * <p>
 *     In this example we setup the server to run a single application that serves static files.
 * </p>
 * <p>
 *     We want to serve the files located in the <code>src/test/resources/examples/fox</code> directory.
 *     We'd like a request to the root of the server to serve the <code>index.html</code> file under that directory.
 * </p>
 * <p>
 *     To see the server's activity, we use the apache common logger middleware to log all requests and status to the
 *     standard output.
 * </p>
 */
public class StaticFilesExample {

    private final Logger logger;

    public StaticFilesExample(Logger logger) {
        this.logger = logger;
    }

    public void run(WebServer server) throws IOException {
        // Setup the file server and tell it to serve files located under the examples/fox directory.
        FileServer files = new FileServer(locateOnClasspath("examples/fox"));
        // Serve static assets (e.g. js files, images, css files, etc) for any request starting at the root path.
        // This will in effect serve every request with static assets.
        // Typically you would specify more specific paths to serve, such as /css, /js, /images, etc.
        StaticAssets assets = new StaticAssets(files).serve("/");
        // If a request targets a directory - the request path ends with "/" -
        // serve the index.html file located in that directory (the default behavior).
        assets.index("index.html");

        // The optional warmup block is executed once at startup as a boot sequence
        server.warmup(app -> logger.info("Ready to serve files"))
              // The apache common logger logs all accesses to the server in apache common log format,
              // so we can see what we're serving.
              .add(new ApacheCommonLogger(logger, Clock.systemDefaultZone(), Locale.ENGLISH))
              .add(assets)
              .start(request -> Response.of(NOT_FOUND).done("Nothing here!"));
    }

    public static void main(String[] args) throws IOException {
        // We choose to log all server accesses to the console
        StaticFilesExample example = new StaticFilesExample(Logging.toConsole());
        WebServer webServer = WebServer.create();
        example.run(webServer);
        System.out.println("Access at " + webServer.uri());
    }
}
