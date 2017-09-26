package examples.middleware;

import com.vtence.molecule.Application;
import com.vtence.molecule.Middleware;
import com.vtence.molecule.Response;
import com.vtence.molecule.WebServer;

import java.io.IOException;

/**
 * <p>
 *     This example demonstrates how to write custom middlewares to add to the processing pipeline. <br>
 *     We demonstrate both performing some work before handling control to processing pipeline
 *     as well as performing additional work once the processing pipeline completes.
 * </p>
 * <p>
 *      Our first middleware reads the <code>User-Agent</code> HTTP header value and
 *      redirects IE users to the Mozilla web site.
 *      <br>
*       The second second middleware calculates the response
 *      size to add a <code>Content-Length</code> HTTP header to the response.
 * </p>
 */
public class CustomMiddlewareExample {

    public void run(WebServer server) throws IOException {
        // To demonstrate performing some work before handling control to the processing pipeline,
        // we read the User-Agent header and redirect IE users to the Mozilla web site.
        Middleware getFirefox = next -> request -> {
                        // Tell IE users to get Firefox
                        String userAgent = request.header("User-Agent");
                        if (userAgent != null && userAgent.contains("MSIE")) {
                            // Short-circuit the processing pipeline and redirect our user
                            // to the Mozilla web site
                            return Response.redirect("http://www.mozilla.org").done();
                        } else {
                            // Hand over control to next application in the stack
                            return next.handle(request);
                        }
                    };

        // To demonstrate performing additional work after getting control back, we set the Content-Length
        // header on the response
        // (for a more capable version of this middleware, check the ContentLengthHeader middleware)
        Middleware contentLengthHeader = next -> request ->
                        // Forward the request for processing, the perform additional work when the response
                        // completes
                        next.handle(request).whenSuccessful(resp -> {
                            // Set the content length header on the response
                            resp.contentLength(resp.size());
                        });

        // A simple hello world application
        Application helloWorld = request ->
                Response.ok()
                        .contentType("text/html")
                        .done("<html><body>Hello, World</body></html>");

        // Deploy middlewares first, followed by our application
        server.add(getFirefox)
              .add(contentLengthHeader)
              .start(helloWorld);
    }

    public static void main(String[] args) throws IOException {
        CustomMiddlewareExample example = new CustomMiddlewareExample();
        // Run the default web server
        WebServer webServer = WebServer.create();
        example.run(webServer);
        System.out.println("Access at " + webServer.uri());
    }
}
