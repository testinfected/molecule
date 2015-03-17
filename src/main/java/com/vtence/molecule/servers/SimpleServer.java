package com.vtence.molecule.servers;

import com.vtence.molecule.Application;
import com.vtence.molecule.Body;
import com.vtence.molecule.BodyPart;
import com.vtence.molecule.FailureReporter;
import com.vtence.molecule.Request;
import com.vtence.molecule.Response;
import com.vtence.molecule.Server;
import org.simpleframework.http.Part;
import org.simpleframework.http.core.Container;
import org.simpleframework.http.core.ContainerServer;
import org.simpleframework.transport.connect.Connection;
import org.simpleframework.transport.connect.SocketConnection;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;

public class SimpleServer implements Server {

    private final String host;
    private final int port;

    private FailureReporter failureReporter = FailureReporter.IGNORE;
    private Connection connection;

    public SimpleServer(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void reportErrorsTo(FailureReporter reporter) {
        this.failureReporter = reporter;
    }

    public int port() {
        return port;
    }

    public String host() {
        return host;
    }

    public void run(final Application app) throws IOException {
        run(app, null);
    }

    public void run(final Application app, SSLContext context) throws IOException {
        connection = new SocketConnection(new ContainerServer(new ApplicationContainer(app)));
        connection.connect(new InetSocketAddress(host, port), context);
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
                Request request = new Request();
                Response response = new Response();
                build(request, simpleRequest);
                app.handle(request, response);
                commit(simpleResponse, response);
            } catch (Throwable failure) {
                failureReporter.errorOccurred(failure);
            } finally {
                close(simpleResponse);
            }
        }

        private void build(Request request, org.simpleframework.http.Request simple) throws IOException {
            setRequestDetails(request, simple);
            setHeaders(request, simple);
            setParameters(request, simple);
            setParts(request, simple);
            setBody(request, simple);
        }

        private void setRequestDetails(Request request, org.simpleframework.http.Request simple) throws IOException {
            request.uri(simple.getTarget());
            request.path(simple.getPath().getPath());
            request.remoteIp(simple.getClientAddress().getAddress().getHostAddress());
            request.remotePort(simple.getClientAddress().getPort());
            request.remoteHost(simple.getClientAddress().getHostName());
            request.timestamp(simple.getRequestTime());
            request.protocol(String.format("HTTP/%s.%s", simple.getMajor(), simple.getMinor()));
            request.secure(simple.isSecure());
            request.method(simple.getMethod());
        }

        private void setHeaders(Request request, org.simpleframework.http.Request simple) {
            List<String> names = simple.getNames();
            for (String header : names) {
                // Apparently there's no way to know the number of values for a given name,
                // so we have to iterate until we reach a null value
                int index = 0;
                while (simple.getValue(header, index) != null) {
                    request.addHeader(header, simple.getValue(header, index));
                    index++;
                }
            }
        }

        private void setParameters(Request request, org.simpleframework.http.Request simple) {
            for (String name : simple.getQuery().keySet()) {
                List<String> values = simple.getQuery().getAll(name);
                for (String value : values) {
                    request.addParameter(name, value);
                }
            }
        }

        private void setParts(Request request, org.simpleframework.http.Request simple) throws IOException {
            for (Part part : simple.getParts()) {
                request.addPart(new BodyPart().content(part.getInputStream())
                                              .contentType(contentTypeOf(part))
                                              .name(part.getName())
                                              .filename(part.getFileName()));
            }
        }

        private String contentTypeOf(Part part) {
            return part.getContentType() != null ? part.getContentType().toString() : null;
        }

        private void setBody(Request request, org.simpleframework.http.Request simple) throws IOException {
            request.body(simple.getInputStream());
        }

        private void commit(org.simpleframework.http.Response simple, Response response) throws IOException {
            setStatusLine(simple, response);
            setHeaders(simple, response);
            writeBody(simple, response);
        }

        private void setStatusLine(org.simpleframework.http.Response simple, Response response) {
            simple.setCode(response.statusCode());
            simple.setDescription(response.statusText());
        }

        private void setHeaders(org.simpleframework.http.Response simple, Response response) {
            for (String name : response.headerNames()) {
                simple.setValue(name, response.header(name));
            }
        }

        private void writeBody(org.simpleframework.http.Response simple, Response response) throws IOException {
            Body body = response.body();
            body.writeTo(simple.getOutputStream(), response.charset());
            body.close();
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