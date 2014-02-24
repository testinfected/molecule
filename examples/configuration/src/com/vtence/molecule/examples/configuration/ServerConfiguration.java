package com.vtence.molecule.examples.configuration;

import com.vtence.molecule.Application;
import com.vtence.molecule.Request;
import com.vtence.molecule.Response;
import com.vtence.molecule.Server;
import com.vtence.molecule.routing.DynamicRoutes;
import com.vtence.molecule.simple.SimpleServer;
import com.vtence.molecule.util.Charsets;
import com.vtence.molecule.util.ConsoleErrorReporter;

import java.io.IOException;

import static com.vtence.molecule.middlewares.Router.draw;

public class ServerConfiguration {

    public static void main(String[] args) throws IOException {
        SimpleServer server = new SimpleServer();
        // Use port 8080, the default being 80
        server.port(8080);
        // Fallback to UTF-8 when no charset is specified in the content type of the response
        server.defaultCharset(Charsets.UTF_8);
        // Report internal errors to the console
        server.reportErrorsTo(ConsoleErrorReporter.toStandardError());
        // Let's shutdown the server properly when the JVM exits
        stopOnExit(server);

        server.run(draw(new DynamicRoutes() {{
            map("/hello").to(new Application() {
                public void handle(Request request, Response response) throws Exception {
                    String contentType = "text/html";
                    String encoding = request.parameter("encoding");
                    if (encoding != null) {
                        // The specified charset will be used automatically to encode the response
                        contentType += "; charset=" + encoding;
                    }
                    // An unsupported charset will cause an exception,
                    // which the failure reporter declared above will catch and log to the console.
                    // If no encoding is given, it will fallback to the server default, in this
                    // case utf-8.
                    response.contentType(contentType);

                    response.body(
                        "<html>" +
                            "<body>" +
                            "<p>" +
                                "Les naïfs ægithales hâtifs pondant à Noël où il gèle sont sûrs " +
                                "d'être déçus en voyant leurs drôles d'œufs abîmés." +
                            "</p>" +
                            "</body>" +
                        "</html>");
                }
            });
        }}));
    }

    private static void stopOnExit(final Server server) {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                try {
                    server.shutdown();
                } catch (Exception ignored) {
                }
            }
        });
    }
}
