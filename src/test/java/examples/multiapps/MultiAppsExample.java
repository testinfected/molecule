package examples.multiapps;

import com.vtence.molecule.WebServer;
import com.vtence.molecule.middlewares.URLMap;

import java.io.IOException;

public class MultiAppsExample {

    public void run(WebServer server) throws IOException {
        URLMap applications = new URLMap();
        applications.mount("/foo", (request, response) -> response.done("/foo  application at " + request.path()))
                    .mount("/foo/bar", (request, response) -> response.done("/foo/bar  application at " + request.path()));

        server.start(applications);
    }

    public static void main(String[] args) throws IOException {
        MultiAppsExample example = new MultiAppsExample();
        // Run the default web server
        WebServer webServer = WebServer.create();
        example.run(webServer);
        System.out.println("Access at " + webServer.uri());
    }
}