package com.vtence.molecule;

import com.vtence.molecule.middlewares.Middleware;
import com.vtence.molecule.middlewares.MiddlewareStack;
import com.vtence.molecule.middlewares.Router;
import com.vtence.molecule.routing.RouteBuilder;
import com.vtence.molecule.simple.SimpleServer;
import com.vtence.molecule.util.FailureReporter;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class WebServer {

    private final SimpleServer server;
    private final MiddlewareStack stack;

    public static WebServer create() {
        return create(SimpleServer.RANDOM_PORT);
    }

    public static WebServer create(int port) {
        return new WebServer(port);
    }

    private WebServer(int port) {
        server = new SimpleServer(port);
        stack = new MiddlewareStack();
    }

    public WebServer failureReporter(FailureReporter reporter) {
        server.reportErrorsTo(reporter);
        return this;
    }

    public WebServer add(Middleware middleware) {
        stack.use(middleware);
        return this;
    }

    public Server start(RouteBuilder routes) throws IOException {
        return start(Router.draw(routes));
    }

    public Server start(Application application) throws IOException {
        stack.run(application);
        server.run(stack);
        return server;
    }

    public void stop() throws IOException {
        server.shutdown();
    }

    public int port() {
        return server.port();
    }

    public String uri() {
        try {
            return "http://" + InetAddress.getLocalHost().getHostName() + ":" + port();
        } catch (UnknownHostException e) {
            throw new HttpException("Cannot figure out server local address", e);
        }
    }
}
