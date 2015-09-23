package examples.multiapps;

import com.vtence.molecule.Application;
import com.vtence.molecule.WebServer;
import com.vtence.molecule.middlewares.URLMap.MountPoint;

import java.io.IOException;

public class MultiAppsExample {

    public void run(WebServer server) throws IOException {

        // This middleware simply prints information about the mount point and request
        Application describe = (request, response) -> {
            // The mount point is available as a request attribute
            MountPoint mount = MountPoint.get(request);
            // The modified request path, stripped of the mounted application prefix
            String pathInfo = request.path();
            // The mounted application path prefix
            String mountPoint = mount.app();
            // To reconstruct the full uri, which is useful when creating links
            String uri = mount.uri(pathInfo);

            response.done(String.format("%s at %s (%s)", mountPoint, pathInfo, uri));
        };

        server.mount("/foo", describe) // matches urls starting with /foo but not /foo/bar
              .mount("/foo/bar", describe) // matches urls starting with /foo/bar
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