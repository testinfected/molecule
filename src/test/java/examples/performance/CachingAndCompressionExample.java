package examples.performance;

import com.vtence.molecule.WebServer;
import com.vtence.molecule.middlewares.Compressor;
import com.vtence.molecule.middlewares.ConditionalGet;
import com.vtence.molecule.middlewares.ContentLengthHeader;
import com.vtence.molecule.middlewares.ETag;
import com.vtence.molecule.middlewares.FileServer;
import com.vtence.molecule.middlewares.StaticAssets;
import com.vtence.molecule.templating.JMustacheRenderer;
import com.vtence.molecule.templating.Template;
import com.vtence.molecule.templating.Templates;
import com.vtence.molecule.testing.ResourceLocator;

import java.io.File;
import java.io.IOException;
import java.time.Clock;

import static com.vtence.molecule.http.HeaderNames.CACHE_CONTROL;
import static com.vtence.molecule.http.HeaderNames.CONTENT_TYPE;
import static com.vtence.molecule.http.HeaderNames.LAST_MODIFIED;
import static com.vtence.molecule.http.MimeTypes.CSS;
import static com.vtence.molecule.http.MimeTypes.HTML;
import static com.vtence.molecule.http.MimeTypes.JAVASCRIPT;

/**
 * <p>
 *     This example shows how to compress responses and do HTTP caching. We serve files files located in the
 *     <code>src/test/resources/examples/fox</code> directory to demonstrate both the
 *     expiration model and validation model (see <a href="http://tools.ietf.org/html/rfc2616">RFC 2616</a>).
 * </p>
 * <p>
 *     We specify how long a response should be considered “fresh” by the client by including both of the
 *     <code>Cache-Control: max-age=N</code> and <code>Expires</code> headers. A client that understands
 *     expiration will not make the same request until the cached version reaches its expiration time
 *     and becomes “stale”.
 * </p>
 * <p>
 *     For more dynamic resources - such as web pages - where changes in resource state can occur frequently and
 *     unpredictably, we use the <code>Last-Modified</code> and <code>ETag</code> headers. A client
 *     that understands cache validators can validate the freshness of its stored response
 *     without requiring our webapp to generate or transmit the response body again.
 * </p>
 */
public class CachingAndCompressionExample {
    private static final Void NO_CONTEXT = null;
    private final Clock clock;

    public CachingAndCompressionExample(Clock clock) {
        this.clock = clock;
    }

    public void run(WebServer server) throws IOException {
        // We serve files located under the examples/fox directory.
        File content = ResourceLocator.locateOnClasspath("examples/fox");
        // Setup the file server with a cache directive of public; max-age=60
        FileServer files = new FileServer(content).header(CACHE_CONTROL, "public; max-age=60");
        // For requests paths starting with /css, /js and /images, we serve static assets
        StaticAssets assets = new StaticAssets(files).serve("/css", "/js", "/images");

        // For other requests, we'll serve web pages.
        // Our web pages are Mustache templates with an .html extension
        Templates templates = new Templates(new JMustacheRenderer().fromDir(content).extension("html"));
        // This is our index.html template
        final Template<Void> index = templates.named("index");

              // Add Content-Length header to the response when size of content is known
        server.add(new ContentLengthHeader())
              // Make GET and HEAD requests conditional to freshness of client stored representation.
              // This is the validation model
              .add(new ConditionalGet())
              // Add an ETag header if response has no freshness information.
              // This is the case for dynamic content, but static files have a Last-Modified header.
              .add(new ETag())
              // Compress text response bodies (js, css, html but not images)
              .add(new Compressor().compressibleTypes(JAVASCRIPT, CSS, HTML))
              // We serve static assets with freshness information. This is the expiration model.
              .add(assets)
              // If client is not requesting a static file, we'll serve our index.html mustache template
              .start((request, response) -> {
                  response.header(CONTENT_TYPE, HTML);
                  // We add freshness information only when query parameter 'conditional' is present
                  if (request.parameter("conditional") != null) {
                      response.header(LAST_MODIFIED, clock.instant());
                  }
                  // This will render our index.html template
                  response.done(index.render(NO_CONTEXT));
              });
    }

    public static void main(String[] args) throws IOException {
        CachingAndCompressionExample example = new CachingAndCompressionExample(Clock.systemDefaultZone());
        WebServer webServer = WebServer.create();
        example.run(webServer);
        // Try accessing /?conditional then / to get a 304
        System.out.println("Access at " + webServer.uri());
    }
}
