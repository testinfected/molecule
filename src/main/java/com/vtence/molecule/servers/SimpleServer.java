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
import org.simpleframework.http.core.ContainerSocketProcessor;
import org.simpleframework.transport.connect.Connection;
import org.simpleframework.transport.connect.SocketConnection;

import javax.net.ssl.SSLContext;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletionException;
import java.util.function.Consumer;

public class SimpleServer implements Server {

    private static final int DEFAULT_NUMBER_OF_THREADS = 8;

    private final String host;
    private final int port;
    private final int numberOfThreads;

    private FailureReporter failureReporter = FailureReporter.IGNORE;
    private Connection connection;

    public SimpleServer(String host, int port) {
        this(host, port, DEFAULT_NUMBER_OF_THREADS);
    }

    public SimpleServer(String host, int port, int numberOfThreads) {
        this.host = host;
        this.port = port;
        this.numberOfThreads = numberOfThreads;
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
        connection = new SocketConnection(new ContainerSocketProcessor(new ApplicationContainer(app), numberOfThreads));
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

        public void handle(org.simpleframework.http.Request httpRequest, org.simpleframework.http.Response httpResponse) {
            final List<Closeable> resources = new ArrayList<>();
            final Request request = new Request();
            final Response response = new Response();
            try {
                read(request, httpRequest, resources);
                app.handle(request, response);
                response.whenSuccessful(commitTo(httpResponse))
                        .whenFailed((result, error) -> failureReporter.errorOccurred(error))
                        .whenComplete((result, error) -> closeAll(resources, httpResponse));
            } catch (Throwable failure) {
                failureReporter.errorOccurred(failure);
                closeAll(resources, httpResponse);
            }
        }

        private void read(Request request, org.simpleframework.http.Request httpRequest,
                          Collection<Closeable> resources) throws IOException {
            readInfo(request, httpRequest);
            readHeaders(request, httpRequest);
            readParameters(request, httpRequest);
            readMultiPartData(request, httpRequest, resources);
            readBody(request, httpRequest, resources);
        }

        private void readInfo(Request request, org.simpleframework.http.Request httpRequest) {
            request.uri(httpRequest.getTarget());
            request.path(httpRequest.getPath().getPath());
            request.remoteIp(httpRequest.getClientAddress().getAddress().getHostAddress());
            request.remotePort(httpRequest.getClientAddress().getPort());
            request.remoteHost(httpRequest.getClientAddress().getHostName());
            request.timestamp(httpRequest.getRequestTime());
            request.protocol(String.format("HTTP/%s.%s", httpRequest.getMajor(), httpRequest.getMinor()));
            request.secure(httpRequest.isSecure());
            request.method(httpRequest.getMethod());
        }

        private void readHeaders(Request request, org.simpleframework.http.Request httpRequest) {
            final List<String> names = httpRequest.getNames();
            for (String header : names) {
                // Apparently there's no way to know the number of values for a given name,
                // so we have to iterate until we reach a null value
                int index = 0;
                while (httpRequest.getValue(header, index) != null) {
                    request.addHeader(header, httpRequest.getValue(header, index));
                    index++;
                }
            }
        }

        private void readParameters(Request request, org.simpleframework.http.Request httpRequest) {
            for (String name : httpRequest.getQuery().keySet()) {
                final List<String> values = httpRequest.getQuery().getAll(name);
                for (String value : values) {
                    request.addParameter(name, value);
                }
            }
        }

        private void readMultiPartData(Request request, org.simpleframework.http.Request httpResponse,
                                       Collection<Closeable> resources) throws IOException {
            for (Part part : httpResponse.getParts()) {
                final InputStream input = part.getInputStream();
                resources.add(input);
                request.addPart(new BodyPart().content(input)
                                              .contentType(contentTypeOf(part))
                                              .name(part.getName())
                                              .filename(part.getFileName()));
            }
        }

        private String contentTypeOf(Part part) {
            return part.getContentType() != null ? part.getContentType().toString() : null;
        }

        private void readBody(Request request, org.simpleframework.http.Request httpResponse,
                              Collection<Closeable> resources) throws IOException {
            final InputStream input = httpResponse.getInputStream();
            resources.add(input);
            request.body(input);
        }

        private Consumer<Response> commitTo(org.simpleframework.http.Response httpResponse) {
            return response -> {
                try {
                    commit(httpResponse, response);
                } catch (IOException e) {
                    throw new CompletionException(e);
                }
            };
        }

        private void commit(org.simpleframework.http.Response httpResponse, Response response) throws IOException {
            writeStatusLine(httpResponse, response);
            writeHeaders(httpResponse, response);
            writeBody(httpResponse, response);
        }

        private void writeStatusLine(org.simpleframework.http.Response httpResponse, Response response) {
            httpResponse.setCode(response.statusCode());
            httpResponse.setDescription(response.statusText());
        }

        private void writeHeaders(org.simpleframework.http.Response httpResponse, Response response) {
            for (String name : response.headerNames()) {
                for (String value: response.headers(name)) {
                    httpResponse.addValue(name, value);
                }
            }
        }

        private void writeBody(org.simpleframework.http.Response httpResponse, Response response) throws IOException {
            final Body body = response.body();
            body.writeTo(httpResponse.getOutputStream(), response.charset());
            body.close();
        }

        // too bad Response does not implement Closeable
        private void closeAll(Iterable<Closeable> resources, org.simpleframework.http.Response httpResponse) {
            for (Closeable resource : resources) {
                close(resource);
            }
            close(httpResponse);
        }

        private void close(org.simpleframework.http.Response httpResponse) {
            try {
                httpResponse.close();
            } catch (IOException e) {
                failureReporter.errorOccurred(e);
            }
        }

        private void close(Closeable resource) {
            try {
                resource.close();
            } catch (IOException e) {
                failureReporter.errorOccurred(e);
            }
        }
    }
}