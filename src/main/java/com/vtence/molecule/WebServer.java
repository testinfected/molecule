package com.vtence.molecule;

import com.vtence.molecule.middlewares.FilterMap;
import com.vtence.molecule.middlewares.Router;
import com.vtence.molecule.routing.RouteBuilder;
import com.vtence.molecule.servers.SimpleServer;

import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.security.GeneralSecurityException;

import static com.vtence.molecule.lib.KeyStoreType.DEFAULT;
import static com.vtence.molecule.lib.SecureProtocol.TLS;

public class WebServer {

    public static final String LOCAL_ADDRESS = "0.0.0.0";
    public static final int DEFAULT_PORT = 8080;

    private final Server server;
    private final MiddlewareStack stack;

    private SSLContext ssl;

    public static WebServer create() {
        return create(DEFAULT_PORT);
    }

    public static WebServer create(int port) {
        return create(LOCAL_ADDRESS, port);
    }

    public static WebServer create(String host, int port) {
        return new WebServer(new SimpleServer(host, port));
    }

    public WebServer(Server server) {
        this.server = server;
        this.stack = new MiddlewareStack();
    }

    public WebServer enableSSL(File keyStore, String storePassword, String keyPassword) throws GeneralSecurityException, IOException {
        return enableSSL(TLS.initialize(DEFAULT.loadKeys(keyStore, storePassword, keyPassword)));
    }

    public WebServer enableSSL(SSLContext context) {
        this.ssl = context;
        return this;
    }

    public WebServer failureReporter(FailureReporter reporter) {
        server.reportErrorsTo(reporter);
        return this;
    }

    public WebServer add(Middleware middleware) {
        stack.use(middleware);
        return this;
    }

    public WebServer filter(String path, Middleware filter) {
        stack.use(new FilterMap().map(path, filter));
        return this;
    }

    public Server start(RouteBuilder routes) throws IOException {
        return start(Router.draw(routes));
    }

    public Server start(Application application) throws IOException {
        server.run(stack.run(application), ssl);
        return server;
    }

    public void stop() throws IOException {
        server.shutdown();
    }

    public URI uri() {
        return URI.create((ssl != null ? "https://" : "http://") + server.host() + ":" + server.port());
    }
}