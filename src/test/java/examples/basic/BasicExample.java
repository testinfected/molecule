package examples.basic;

import com.vtence.molecule.Application;
import com.vtence.molecule.Request;
import com.vtence.molecule.Response;
import com.vtence.molecule.WebServer;
import com.vtence.molecule.util.ConsoleErrorReporter;

import java.io.IOException;

public class BasicExample {

    public void run(WebServer server) throws IOException {
        // Report uncaught exceptions to the standard error stream
        server.failureReporter(ConsoleErrorReporter.toStandardError());
        server.start(new Application() {
            public void handle(Request request, Response response) throws Exception {
                String encoding = request.parameter("encoding");
                // The specified charset will be used automatically to encode the response
                String contentType = "text/html; charset=" + encoding;
                // An unsupported charset will cause an exception,
                // which the failure reporter declared above will catch and log to the console.
                response.contentType(contentType);

                response.body(
                        "<html>" +
                            "<body>" +
                                "<p>" +
                                "Les naïfs ægithales hâtifs pondant à Noël où il gèle sont sûrs " +
                                "d'être déçus en voyant leurs drôles d'œufs abîmés." +
                                "</p>" +
                            "</body>" +
                        "</html>"
                );
            }
        });
    }


    public static void main(String[] args) throws IOException {
        // Run server on a random available port
        WebServer webServer = WebServer.create();
        new BasicExample().run(webServer);
        System.out.println("Running on " + webServer.uri());
    }
}
