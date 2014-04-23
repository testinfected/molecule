package examples.simple;

import com.vtence.molecule.Application;
import com.vtence.molecule.Request;
import com.vtence.molecule.Response;
import com.vtence.molecule.Server;
import com.vtence.molecule.routing.DynamicRoutes;
import com.vtence.molecule.simple.SimpleServer;
import com.vtence.molecule.util.ConsoleErrorReporter;

import java.io.IOException;
import java.net.InetAddress;

import static com.vtence.molecule.middlewares.Router.draw;

public class SimpleExample {

    public void run(Server server) throws IOException {
        server.run(draw(new DynamicRoutes() {{
            map("/pangram").to(new Application() {
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
        }}));
    }

    public static void main(String[] args) throws IOException {
        // By default, server will run on a random available port...
        SimpleServer server = new SimpleServer();
        // Report internal errors to the console
        server.reportErrorsTo(ConsoleErrorReporter.toStandardError());
        new SimpleExample().run(server);
        System.out.println("Running on http://" + InetAddress.getLocalHost().getHostName() + ":" + server.port());
    }

}
