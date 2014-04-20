package com.vtence.molecule.simple;

import com.vtence.molecule.Application;
import com.vtence.molecule.Body;
import com.vtence.molecule.Cookie;
import com.vtence.molecule.Response;
import com.vtence.molecule.Server;
import com.vtence.molecule.util.FailureReporter;
import org.simpleframework.http.Request;
import org.simpleframework.http.core.Container;
import org.simpleframework.http.core.ContainerServer;
import org.simpleframework.transport.connect.Connection;
import org.simpleframework.transport.connect.SocketConnection;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.List;
import java.util.Map;

public class SimpleServer implements Server {

    private static final int RANDOM_PORT = 0;

    private int port;
    private FailureReporter failureReporter = FailureReporter.IGNORE;

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

        public void handle(org.simpleframework.http.Request simpleRequest, org.simpleframework.http.Response simpleResponse) {
            try {
                SimpleRequest request = new SimpleRequest(simpleRequest);
                Response response = new Response();
                build(simpleRequest, request);
                app.handle(request, response);
                commit(simpleResponse, response);
            } catch (Exception failure) {
                failureReporter.errorOccurred(failure);
            } finally {
                close(simpleResponse);
            }
        }

        private void build(org.simpleframework.http.Request simple, SimpleRequest request) throws IOException {
            request.uri(simple.getTarget());
            request.path(simple.getPath().getPath());
            request.remoteIp(simple.getClientAddress().getAddress().getHostAddress());
            request.remotePort(simple.getClientAddress().getPort());
            request.remoteHost(simple.getClientAddress().getHostName());
            request.protocol(String.format("HTTP/%s.%s", simple.getMajor(), simple.getMinor()));
            request.input(simple.getInputStream());
            request.method(simple.getMethod());
            buildHeaders(simple, request);
            buildCookies(simple, request);
            buildParameters(simple, request);
            buildAttributes(simple, request);
        }

        @SuppressWarnings("unchecked")
        private void buildAttributes(Request simple, SimpleRequest request) {
            Map<Object, Object> attributes = simple.getAttributes();
            for (Object key : attributes.keySet()) {
                request.attribute(key, attributes.get(key));
            }
        }

        private void buildHeaders(org.simpleframework.http.Request simple, SimpleRequest request) {
            List<String> names = simple.getNames();
            for (String header : names) {
                List<String> values = simple.getValues(header);
                for (String value : values) {
                    request.addHeader(header, value);
                }
            }
        }

        private void buildCookies(org.simpleframework.http.Request simpleRequest, SimpleRequest request) {
            for (org.simpleframework.http.Cookie cookie : simpleRequest.getCookies()) {
                request.addCookie(cookie.getName(), cookie.getValue());
            }
        }

        private void buildParameters(Request simple, SimpleRequest request) {
            for (String name : simple.getQuery().keySet()) {
                List<String> values = simple.getQuery().getAll(name);
                for (String value : values) {
                    request.addParameter(name, value);
                }
            }
        }

        private void commit(org.simpleframework.http.Response simple, Response response) throws IOException {
            simple.setCode(response.statusCode());
            simple.setDescription(response.statusText());
            commitHeaders(simple, response);
            commitCookies(simple, response);
            Body body = response.body();
            body.writeTo(simple.getOutputStream(), response.charset());
            body.close();
        }

        private void commitHeaders(org.simpleframework.http.Response simple, Response response) {
            for (String name : response.names()) {
                simple.setValue(name, response.get(name));
            }
        }

        private void commitCookies(org.simpleframework.http.Response simple, Response response) {
            for (Cookie cookie : response.cookies()) {
                org.simpleframework.http.Cookie cooky = simple.setCookie(cookie.name(), cookie.value());
                cooky.setProtected(cookie.httpOnly());
                cooky.setExpiry(cookie.maxAge());
            }
        }

        private void close(org.simpleframework.http.Response response) {
            try {
                response.close();
            } catch (IOException e) {
                failureReporter.errorOccurred(e);
            }
        }
    }
}