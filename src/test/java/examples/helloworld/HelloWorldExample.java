package examples.helloworld;

import com.vtence.molecule.Application;
import com.vtence.molecule.Request;
import com.vtence.molecule.Response;
import com.vtence.molecule.Server;
import com.vtence.molecule.simple.SimpleServer;

import java.io.IOException;

public class HelloWorldExample {

    private final Server server;

    public HelloWorldExample(int port) {
        this.server = new SimpleServer(port);
    }

    public void start() throws IOException {
        server.run(new Application() {
            public void handle(Request request, Response response) throws Exception {
                response.body("Hello, World");
            }
        });
    }

    public void stop() throws IOException {
        server.shutdown();
    }
}


