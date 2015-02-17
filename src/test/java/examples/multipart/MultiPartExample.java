package examples.multipart;

import com.vtence.molecule.Application;
import com.vtence.molecule.Request;
import com.vtence.molecule.Response;
import com.vtence.molecule.WebServer;

import java.io.IOException;

public class MultiPartExample {

    public void run(WebServer server) throws IOException {
        server.start(new Application() {
            @Override
            public void handle(Request request, Response response) throws Exception {
                String say = request.part("say").value();
                String to = request.part("to").value();
                response.body(say + " " + to);
            }
        });
    }

    public static void main(String[] args) throws IOException {
        // Let's log server access to the console, so we can see content we're serving
        MultiPartExample example = new MultiPartExample();
        WebServer webServer = WebServer.create();
        example.run(webServer);
        System.out.println("Access at " + webServer.uri());
    }}
