package examples.multiapps;

import com.vtence.molecule.Application;
import com.vtence.molecule.WebServer;
import com.vtence.molecule.middlewares.URLMap.MountPoint;

import java.io.IOException;

public class MultiAppsExample {

    public void run(WebServer server) throws IOException {

        Application describe = (request, response) -> {
            MountPoint mount = MountPoint.get(request);
            String pathInfo = request.path();
            String mountPoint = mount.app();
            String uri = mount.uri(pathInfo);

            response.done(String.format("%s at %s (%s)", mountPoint, pathInfo, uri));
        };

        server.mount("/foo", describe)
              .mount("/foo/bar", describe)
              .mount("/baz", describe)
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