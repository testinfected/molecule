package examples.performance;

import com.vtence.molecule.Application;
import com.vtence.molecule.Request;
import com.vtence.molecule.Response;
import com.vtence.molecule.WebServer;
import com.vtence.molecule.middlewares.Compressor;
import com.vtence.molecule.middlewares.ConditionalGet;
import com.vtence.molecule.middlewares.ContentLengthHeader;
import com.vtence.molecule.middlewares.ETag;
import com.vtence.molecule.middlewares.FileServer;
import com.vtence.molecule.middlewares.StaticAssets;
import com.vtence.molecule.support.ResourceLocator;
import com.vtence.molecule.templating.JMustacheRenderer;
import com.vtence.molecule.templating.Template;
import com.vtence.molecule.templating.Templates;

import java.io.File;
import java.io.IOException;

import static com.vtence.molecule.http.HeaderNames.CACHE_CONTROL;
import static com.vtence.molecule.http.HeaderNames.CONTENT_TYPE;
import static com.vtence.molecule.http.HeaderNames.LAST_MODIFIED;
import static com.vtence.molecule.http.MimeTypes.CSS;
import static com.vtence.molecule.http.MimeTypes.HTML;
import static com.vtence.molecule.http.MimeTypes.JAVASCRIPT;

public class CachingAndCompressionExample {
    private static final Object NO_CONTEXT = null;

    public void run(WebServer server) throws IOException {
        File content = ResourceLocator.locateOnClasspath("examples/fox");
        // Add cache directives to the response when serving files
        FileServer files = new FileServer(content).header(CACHE_CONTROL, "public; max-age=60");
        // Serve static assets for css, js and image files from the content dir.
        StaticAssets assets = new StaticAssets(files).serve("/css", "/js", "/images");
        // We use Mustache templates with an .html extension
        Templates templates = new Templates(new JMustacheRenderer().fromDir(content).extension("html"));
        final Template index = templates.named("index");

              // Add content length header when size of content is known
        server.add(new ContentLengthHeader())
              // Make get and head requests conditional to freshness of client stored representation
              .add(new ConditionalGet())
              // Add ETag if response has no validation information
              .add(new ETag())
              // Compress bodies that are not images
              .add(new Compressor().compressibleTypes(JAVASCRIPT, CSS, HTML))
              .add(assets)
              .start(new Application() {
                  public void handle(Request request, Response response) throws Exception {
                      response.set(CONTENT_TYPE, HTML);
                      response.set(LAST_MODIFIED, request.parameter("timestamp"));
                      response.body(index.render(NO_CONTEXT));
                  }
              });
    }

    public static void main(String[] args) throws IOException {
        CachingAndCompressionExample example = new CachingAndCompressionExample();
        WebServer webServer = WebServer.create();
        example.run(webServer);
        System.out.println("Access at " + webServer.uri());
    }
}
