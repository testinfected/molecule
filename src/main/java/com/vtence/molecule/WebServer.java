package com.vtence.molecule;

import com.vtence.molecule.middlewares.FilterMap;
import com.vtence.molecule.middlewares.Router;
import com.vtence.molecule.routing.RouteBuilder;
import com.vtence.molecule.servers.Servers;

import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.security.GeneralSecurityException;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static com.vtence.molecule.ssl.KeyStoreType.DEFAULT;
import static com.vtence.molecule.ssl.SecureProtocol.TLS;

public class WebServer {

    private static final String LOCAL_ADDRESS = "0.0.0.0";
    private static final int DEFAULT_PORT = 8080;

    private final Server server;
    private final MiddlewareStack stack;
    private final Set<ServerOption> options = new HashSet<>();

    private SSLContext ssl;

    /**
     * Creates a WebServer listening on the default interface (0.0.0.0) and port (8080).
     */
    public static WebServer create() {
        return create(DEFAULT_PORT);
    }

    /**
     * Creates a WebServer listening on the default interface (0.0.0.0) and the specified port.
     *
     * @param port the port to listen on
     */
    public static WebServer create(int port) {
        return create(LOCAL_ADDRESS, port);
    }

    /**
     * Creates a WebServer listening on the specified interface and port.
     *
     * @param host the hostname to bind to
     * @param port the port to listen on
     */
    public static WebServer create(String host, int port) {
        return new WebServer(Servers.create(host, port));
    }

    /**
     * Creates a WebServer with the specified server instance.
     *
     * @param server the server instance to use
     */
    public WebServer(Server server) {
        this.server = server;
        this.stack = new MiddlewareStack();
    }

    /**
     * Secures connections made to this WebServer with TLS using the specified key store and credentials.
     * The key store must be of the default platform type and use the default key manager algorithm.
     *
     * @param keyStore      the location of the key store containing the certificate and keys
     * @param storePassword the password that opens the key store
     * @param keyPassword   the password for using the keys
     * @see com.vtence.molecule.ssl.SecureProtocol
     * @see com.vtence.molecule.ssl.KeyStoreType
     */
    public WebServer enableSSL(File keyStore, String storePassword, String keyPassword)
            throws GeneralSecurityException, IOException {
        return enableSSL(TLS.initialize(DEFAULT.loadKeys(keyStore, storePassword, keyPassword)));
    }

    /**
     * Secures connections made to this WebServer using the provided security context. This allows to use
     * security settings that differ from platform defaults (such as using PKCS12 instead of JKS).
     *
     * @param context the security context for securing connections
     * @see com.vtence.molecule.ssl.SecureProtocol
     * @see com.vtence.molecule.ssl.KeyStoreType
     */
    public WebServer enableSSL(SSLContext context) {
        this.ssl = context;
        return this;
    }

    /**
     * Prefers to use HTTP/2 over HTTP/1.1 if the underlying server supports it.
     */
    public WebServer enableHTTP2() {
        this.options.add(ServerOption.HTTP_2);
        return this;
    }

    /**
     * Notifies the given error consumer when an uncaught exceptions occurs.
     *
     * @param reporter the failure reporter to notify in case of uncaught exceptions
     */
    public WebServer failureReporter(FailureReporter reporter) {
        server.reportErrorsTo(reporter);
        return this;
    }

    /**
     * Adds a middleware to this WebServer's stack of configured middlewares.
     *
     * @param middleware the middleware to add to the stack
     */
    public WebServer add(Middleware middleware) {
        stack.use(middleware);
        return this;
    }

    /**
     * Adds a middleware filter to this WebServer's stack of configured middlewares. The server will apply the
     * given filter to all requests with a path starting with the specified prefix.
     *
     * @param path   the path to trigger filtering
     * @param filter the filter to apply to incoming requests that match the path prefix
     */
    public WebServer filter(String path, Middleware filter) {
        stack.use(new FilterMap().map(path, filter));
        return this;
    }

    /**
     * Adds a middleware filter to this WebServer's stack of configured middlewares. The server will apply the given
     * filter to all requests that satisfy the specified condition.
     *
     * @param condition the condition to trigger filtering
     * @param filter    the filter to apply to incoming requests that are matched
     */
    public WebServer filter(Predicate<? super Request> condition, Middleware filter) {
        stack.use(new FilterMap().map(condition, filter));
        return this;
    }

    /**
     * Mounts the specified middleware stack at the given path. This WebServer will route all incoming requests
     * which target that path or any sub-path to that stack.
     *
     * @param path the mount point
     * @param stack the middleware stack to attach to the mount point
     */
    public WebServer mount(String path, MiddlewareStack stack) {
        return mount(path, stack.boot());
    }

    /**
     * Mounts the specified application at the given path. This WebServer will route all incoming requests
     * which target that path or any sub-path to the specified application.
     *
     * @param path the mount point
     * @param app  the application to attach to the mount point
     */
    public WebServer mount(String path, Application app) {
        stack.mount(path, app);
        return this;
    }

    /**
     * Configures an optional warmup sequence to run once at this WebServer startup.
     *
     * @param warmup the warmup sequence to boot the application
     */
    public WebServer warmup(Consumer<Application> warmup) {
        stack.warmup(warmup);
        return this;
    }

    /**
     * Boots and starts this WebServer using the previously configured middlewares and create a router
     * with the specified routes. The router will run at the root mount point.
     * <p>
     * The server will start accepting and processing incoming requests.
     *
     * @param routes the routes to run at the root mount point (/)
     */
    public Server route(RouteBuilder routes) throws IOException {
        return start(Router.draw(routes));
    }

    /**
     * Boots and starts this WebServer using the previously configured middlewares
     * and the specified middleware stack at the root the mount point.
     * <p>
     * The server will start accepting and processing incoming requests.
     *
     * @param stack the middleware stack to run at the root mount point (/)
     */
    public Server start(MiddlewareStack stack) throws IOException {
        return start(stack.boot());
    }

    /**
     * Boots and starts this WebServer using the previously configured middlewares
     * and the specified application at the root the mount point.
     * <p>
     * The server will start accepting and processing incoming requests.
     *
     * @param application the application to run at the root mount point (/)
     */
    public Server start(Application application) throws IOException {
        stack.run(application);
        return start();
    }

    /**
     * Boots and starts this WebServer using the previously configured middlewares and mount points.
     * The server will start accepting and processing incoming requests.
     * <p>
     * <i>Note that you need to configure at least a mount point before starting the server.</i>
     * </p>
     *
     * @see WebServer#mount(String, Application)
     */
    public Server start() throws IOException {
        if (ssl != null) {
            server.run(stack.boot(), ssl, options.toArray(new ServerOption[0]));
        } else {
            server.run(stack.boot(), options.toArray(new ServerOption[0]));
        }
        return server;
    }

    /**
     * Stops this WebServer and releases its resources. The server will no longer accept or process incoming
     * requests.
     */
    public void stop() throws IOException {
        server.shutdown();
    }

    /**
     * Returns the uri of this WebServer root.
     *
     * @return the server root URI
     */
    public URI uri() {
        return URI.create((ssl != null ? "https://" : "http://") + server.host() + ":" + server.port());
    }
}