package examples.middlewares;

import com.vtence.molecule.Application;
import com.vtence.molecule.Request;
import com.vtence.molecule.Response;
import com.vtence.molecule.WebServer;
import com.vtence.molecule.lib.AbstractMiddleware;
import com.vtence.molecule.lib.Middleware;

import java.io.IOException;

public class MiddlewaresExample {

    public void run(WebServer server) throws IOException {

        // An example of performing some work before handling control to the next or application
        Middleware getFirefox = new AbstractMiddleware() {
            public void handle(Request request, Response response) throws Exception {
                // Tell IE users to get Firefox
                String userAgent = request.header("User-Agent");
                if (userAgent != null && userAgent.contains("MSIE")) {
                    response.redirectTo("http://www.mozilla.org");
                } else {
                    // Hand over control to next application in the stack
                    forward(request, response);
                }
            }
        };

        // An example of performing additional work after getting control back
        // (there's already a middleware for that, btw)
        Middleware contentLengthHeader = new AbstractMiddleware() {
            public void handle(Request request, Response response) throws Exception {
                forward(request, response);
                // Set content length header on the response
                response.contentLength(response.size());
            }
        };


        // A simple hello world application
        Application helloWorld = new Application() {
            public void handle(Request request, Response response) throws Exception {
                response.contentType("text/html").body("<html><body>Hello, World</body></html>");
            }
        };

        // Deploy middlewares first, followed by our application
        server.add(getFirefox)
              .add(contentLengthHeader)
              .start(helloWorld);
    }

    public static void main(String[] args) throws IOException {
        WebServer webServer = WebServer.create();
        MiddlewaresExample example = new MiddlewaresExample();
        example.run(webServer);
        System.out.println("Access at " + webServer.uri());
    }
}
