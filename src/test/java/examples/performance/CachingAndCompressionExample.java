package examples.performance;

import com.vtence.molecule.WebServer;
import com.vtence.molecule.lib.Clock;
import com.vtence.molecule.lib.SystemClock;
import com.vtence.molecule.middlewares.*;
import com.vtence.molecule.templating.JMustacheRenderer;
import com.vtence.molecule.templating.Template;
import com.vtence.molecule.templating.Templates;
import com.vtence.molecule.testing.ResourceLocator;

import java.io.File;
import java.io.IOException;

import static com.vtence.molecule.http.HeaderNames.*;
import static com.vtence.molecule.http.HttpDate.httpDate;
import static com.vtence.molecule.http.MimeTypes.*;

public class CachingAndCompressionExample {
    private static final Object NO_CONTEXT = null;
    private final Clock clock;

    public CachingAndCompressionExample(Clock clock) {
        this.clock = clock;
    }

    public void run(WebServer server) throws IOException {
        File content = ResourceLocator.locateOnClasspath("examples/fox");
        // Add cache directives to the response when serving files
        FileServer files = new FileServer(content).header(CACHE_CONTROL, "public; max-age=60");
        // Serve static assets for css, js and image files from the content dir
        StaticAssets assets = new StaticAssets(files).serve("/css", "/js", "/images");
        // We use Mustache templates with an .html extension
        Templates templates = new Templates(new JMustacheRenderer().fromDir(content).extension("html"));
        final Template<Object> index = templates.named("index");

              // Add content length header when size of content is known
        server.add(new ContentLengthHeader())
              // Make get and head requests conditional to freshness of client stored representation
              .add(new ConditionalGet())
              // Add ETag if response has no freshness information
              .add(new ETag())
              // Compress bodies that are not images
              .add(new Compressor().compressibleTypes(JAVASCRIPT, CSS, HTML))
              .add(assets)
              .start((request, response) -> {
                  response.header(CONTENT_TYPE, HTML);
                  // Add freshness information only when conditional parameter is set
                  if (request.parameter("conditional") != null) {
                      response.header(LAST_MODIFIED, httpDate(clock.now()));
                  }
                  response.body(index.render(NO_CONTEXT))
                          .done();
              });
    }

    public static void main(String[] args) throws IOException {
        CachingAndCompressionExample example = new CachingAndCompressionExample(new SystemClock());
        WebServer webServer = WebServer.create();
        example.run(webServer);
        System.out.println("Access at " + webServer.uri());
    }
}