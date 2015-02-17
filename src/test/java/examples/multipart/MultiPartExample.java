package examples.multipart;

import com.vtence.molecule.Application;
import com.vtence.molecule.BodyPart;
import com.vtence.molecule.Request;
import com.vtence.molecule.Response;
import com.vtence.molecule.WebServer;
import com.vtence.molecule.routing.DynamicRoutes;

import java.io.IOException;
import java.util.List;

public class MultiPartExample {

    public void run(WebServer server) throws IOException {
        server.start(new DynamicRoutes() {{
            post("/greeting").to(new Application() {
                public void handle(Request request, Response response) throws Exception {
                    String say = request.part("say").text();
                    String to = request.part("to").text();
                    response.body(say + " " + to);
                }
            });

            post("/biography").to(new Application() {
                public void handle(Request request, Response response) throws Exception {
                    List<BodyPart> parts = request.parts();
                    // Ignore all but the last part. In our example we submit a single one
                    for (BodyPart part : parts) {
                        response.contentType(part.contentType());
                        response.body(part.text());
                    }
                }
            });
        }});
    }

    public static void main(String[] args) throws IOException {
        MultiPartExample example = new MultiPartExample();
        WebServer webServer = WebServer.create();
        example.run(webServer);
        System.out.println("Access at " + webServer.uri());
    }}
