package com.vtence.molecule.examples.files;

import com.vtence.molecule.middlewares.FileServer;
import com.vtence.molecule.middlewares.StaticAssets;
import com.vtence.molecule.simple.SimpleServer;

import java.io.File;
import java.io.IOException;

/**
 * Access at <a href="http://localhost:8080">http://localhost:8080</a>
 */
public class StaticFiles {

    public static void main(String[] args) throws IOException {
        SimpleServer server = new SimpleServer(8080);

        // Serve files in the examples/files/blog directory, according to
        // the path info of the request.
        FileServer files = new FileServer(new File("./examples/files/site"));

        // Serve static assets (e.g. js files, images, css files, etc)
        // for any request whose path starts with one of the url prefixes specified
        // (in this case, "/").
        StaticAssets assets = new StaticAssets(files).serve("/");

        // If request targets a directory (i.e. request path ends with "/"),
        // serve the index.html file located in that directory.
        assets.index("index.html");

        server.run(assets);
    }
}
