package examples.multiapps;

import com.vtence.molecule.Application;
import com.vtence.molecule.Response;
import com.vtence.molecule.WebServer;
import com.vtence.molecule.middlewares.URLMap.MountPoint;

import java.io.IOException;


/**
 * <p>
 *     In this example we demonstrate attaching several applications to different mount points. Mount points are
 *     matched as words in definition order. More specific paths have precedence over less specific ones.
 * </p>
 * <p>
 *     This is useful when you want multiple applications to coexist on the server. For instance, you could run both an
 *     API application under /api and a web application under /.
 * </p>
 */
public class MultiAppsExample {

    public void run(WebServer server) throws IOException {
        // We will mount a simple application that prints information about the mount point and client request
        Application describe = request -> {
            // The mount point is available as a request attribute
            MountPoint mount = MountPoint.get(request);
            // The mounted application path prefix (i.e. either /foo, /foo/bar or /baz in our example)
            String mountPoint = mount.app();
            // The modified request path, stripped of the mounted application prefix
            // (e.g. this will be /baz for a request to /foo/bar/baz if the mount point is /foo/bar)
            String pathInfo = request.path();
            // To reconstruct the full uri, which is useful when creating links
            String uri = mount.uri(pathInfo);

            return Response.ok()
                           .done(String.format("%s at %s (%s)", mountPoint, pathInfo, uri));
        };

        // Mount points are matched in definition order, the most specific match wins
        server.mount("/foo", describe) // matches urls starting with /foo but not /foo/bar
              .mount("/foo/bar", describe) // matches urls starting with /foo/bar - wins over /foo since more specific
              .mount("/baz", describe) // matches urls starting with /baz
              // Start the server without a default app, which means all other requests will get a 404
              .start();
    }

    public static void main(String[] args) throws IOException {
        MultiAppsExample example = new MultiAppsExample();
        // Run the default web server
        WebServer webServer = WebServer.create();
        example.run(webServer);
        System.out.println("Access at " + webServer.uri());
    }
}
