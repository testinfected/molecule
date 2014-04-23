package examples.helloworld;

import com.vtence.molecule.Application;
import com.vtence.molecule.Request;
import com.vtence.molecule.Response;
import com.vtence.molecule.Server;
import com.vtence.molecule.simple.SimpleServer;

import java.io.IOException;
import java.net.InetAddress;

public class HelloWorldExample {

    public void run(Server server) throws IOException {
        server.run(new Application() {
            public void handle(Request request, Response response) throws Exception {
                response.body("Hello, World");
            }
        });
    }

    public static void main(String[] args) throws IOException {
        // By default, server will run on a random available port...
        SimpleServer server = new SimpleServer();
        new HelloWorldExample().run(server);
        System.out.println("Running on http://" + InetAddress.getLocalHost().getHostName() + ":" + server.port());
    }
}