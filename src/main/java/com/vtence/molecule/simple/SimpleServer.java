package com.vtence.molecule.simple;

import com.vtence.molecule.Application;
import com.vtence.molecule.Body;
import com.vtence.molecule.Server;
import com.vtence.molecule.simple.session.DisableSessions;
import com.vtence.molecule.simple.session.SessionTracker;
import com.vtence.molecule.simple.session.SessionTracking;
import com.vtence.molecule.util.FailureReporter;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.core.Container;
import org.simpleframework.http.core.ContainerServer;
import org.simpleframework.transport.connect.Connection;
import org.simpleframework.transport.connect.SocketConnection;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

public class SimpleServer implements Server {

    private static final int RANDOM_PORT = 0;

    private int port;
    private FailureReporter failureReporter = FailureReporter.IGNORE;
    private SessionTracker tracker = new DisableSessions();

    private Connection connection;

    public SimpleServer() {
        this(RANDOM_PORT);
    }

    public SimpleServer(int port) {
        this.port = port;
    }

    public void reportErrorsTo(FailureReporter reporter) {
        this.failureReporter = reporter;
    }

    public void enableSessions(SessionTracker tracker) {
        this.tracker = tracker;
    }

    public void port(int port) {
        this.port = port;
    }

    public int port() {
        return port;
    }

    public void run(final Application app) throws IOException {
        connection = new SocketConnection(new ContainerServer(new ApplicationContainer(app)));
        SocketAddress address = new InetSocketAddress(port);
        // The actual port the server is running on, in case we're using any random available port
        port = ((InetSocketAddress) connection.connect(address)).getPort();
    }

    public void shutdown() throws IOException {
        if (connection != null) connection.close();
    }

    public class ApplicationContainer implements Container {
        private final Application app;

        public ApplicationContainer(Application app) {
            this.app = app;
        }

        public void handle(Request req, Response resp) {
            try {
                // todo progressively morph our request and response from adapters of their
                // simpleweb counterparts to state containers. Once this is done, we will
                // make our request and response concrete classes.
                SimpleResponse response = new SimpleResponse(resp);
                SimpleRequest request = new SimpleRequest(req, new SessionTracking(tracker, response));

                app.handle(request, response);

                // todo After processing is done, sets headers, status and body to the simple
                // response
                Body body = response.body();
                body.writeTo(resp.getOutputStream());
                body.close();
            } catch (Exception failure) {
                failureReporter.errorOccurred(failure);
            } finally {
                close(resp);
            }
        }

        private void close(Response response) {
            try {
                response.close();
            } catch (IOException e) {
                failureReporter.errorOccurred(e);
            }
        }
    }
}