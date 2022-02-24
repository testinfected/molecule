package com.vtence.molecule.servers;

import com.vtence.molecule.Application;
import com.vtence.molecule.Body;
import com.vtence.molecule.BodyPart;
import com.vtence.molecule.FailureReporter;
import com.vtence.molecule.Request;
import com.vtence.molecule.Response;
import com.vtence.molecule.Server;
import com.vtence.molecule.ServerOption;
import com.vtence.molecule.helpers.Headers;
import com.vtence.molecule.http.HttpMethod;
import com.vtence.molecule.http.Uri;
import org.simpleframework.http.Part;
import org.simpleframework.http.Query;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionException;
import java.util.function.Consumer;

import static com.vtence.molecule.http.HttpMethod.valueOf;
import static com.vtence.molecule.http.Scheme.HTTP;
import static com.vtence.molecule.http.Scheme.HTTPS;

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

    public void run(final Application app, ServerOption... options) throws IOException {
        run(app, null, options);
    }

    public void run(final Application app, SSLContext context, ServerOption... options) throws IOException {
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

        public void handle(org.simpleframework.http.Request req, org.simpleframework.http.Response resp) {
            new RequestHandler(app).handle(req, resp);
        }
    }

    private class RequestHandler implements Container {
        private final List<Closeable> resources = new ArrayList<>();
        private final Application app;

        public RequestHandler(Application app) {
            this.app = app;
        }

        public void handle(org.simpleframework.http.Request req, org.simpleframework.http.Response resp) {
            try {
                app.handle(asRequest(req))
                   .whenSuccessful(transferTo(resp))
                   .whenFailed((result, error) -> failureReporter.errorOccurred(error))
                   .whenComplete((result, error) -> closeAll(resp));
            } catch (Throwable failure) {
                failureReporter.errorOccurred(failure);
                closeAll(resp);
            }
        }

        private Request asRequest(org.simpleframework.http.Request req) throws IOException {
            return makeRequest(req)
                    .remoteIp(req.getClientAddress().getAddress().getHostAddress())
                    .remotePort(req.getClientAddress().getPort())
                    .remoteHost(req.getClientAddress().getHostName())
                    .timestamp(req.getRequestTime())
                    .protocol(String.format("HTTP/%s.%s", req.getMajor(), req.getMinor()))
                    .secure(req.isSecure())
                    .body(readBody(req));
        }

        private Request makeRequest(org.simpleframework.http.Request req) throws IOException {
            return new Request(getMethod(req),
                               reconstructUri(req),
                               getProtocol(req),
                               readHeaders(req),
                               readParameters(req),
                               readMultiPartData(req));
        }

        private String getProtocol(org.simpleframework.http.Request req) {
            return String.format("HTTP/%d.%d", req.getMajor(), req.getMinor());
        }

        private HttpMethod getMethod(org.simpleframework.http.Request req) {
            return valueOf(req.getMethod());
        }

        private Uri reconstructUri(org.simpleframework.http.Request req) {
            Uri uri = Uri.of(req.getTarget());
            if (uri.scheme() == null) uri = uri.scheme(req.isSecure() ? HTTPS.name() : HTTP.name());
            if (uri.host() == null) uri = uri.host(host);
            if (uri.port() == -1) uri = uri.port(port);
            return uri;
        }

        private Headers readHeaders(org.simpleframework.http.Request req) {
            Headers headers = new Headers();
            for (String header : req.getNames()) {
                // Apparently there's no way to know the number of values for a given name,
                // so we have to iterate until we reach a null value
                int index = 0;
                while (req.getValue(header, index) != null) {
                    headers.add(header, req.getValue(header, index));
                    index++;
                }
            }
            return headers;
        }

        private Map<String, List<String>> readParameters(org.simpleframework.http.Request req) {
            Map<String, List<String>> parameters = new HashMap<>();
            Query query = req.getQuery();
            query.keySet().forEach(name -> {
                query.getAll(name).forEach(
                        value -> parameters.computeIfAbsent(name, k -> new ArrayList<>())
                                           .add(value));
            });
            return parameters;
        }

        private List<BodyPart> readMultiPartData(org.simpleframework.http.Request req) throws IOException {
            List<BodyPart> parts = new ArrayList<>();
            for (Part part : req.getParts()) {
                parts.add(new BodyPart().content(track(part.getInputStream()))
                                        .contentType(contentTypeOf(part))
                                        .name(part.getName())
                                        .filename(part.getFileName()));
            }
            return parts;
        }

        private String contentTypeOf(Part part) {
            return part.getContentType() != null ? part.getContentType().toString() : null;
        }

        private InputStream readBody(org.simpleframework.http.Request req) throws IOException {
            return track(req.getInputStream());
        }

        private Consumer<Response> transferTo(org.simpleframework.http.Response httpResponse) {
            return response -> {
                try {
                    commit(httpResponse, response);
                } catch (IOException e) {
                    throw new CompletionException(e);
                }
            };
        }

        private void commit(org.simpleframework.http.Response httpResponse, Response response) throws IOException {
            setStatusLine(httpResponse, response);
            setHeaders(httpResponse, response);
            writeBody(httpResponse, response);
        }

        private void setStatusLine(org.simpleframework.http.Response httpResponse, Response response) {
            httpResponse.setCode(response.statusCode());
            httpResponse.setDescription(response.statusDescription());
        }

        private void setHeaders(org.simpleframework.http.Response httpResponse, Response response) {
            response.headerNames().forEach(
                    name -> response.headers(name).forEach(
                            value -> httpResponse.addValue(name, value)));
        }

        private void writeBody(org.simpleframework.http.Response httpResponse, Response response) throws IOException {
            try(Body body = response.body()) {
                body.writeTo(httpResponse.getOutputStream(), response.charset());
            }
        }

        private <T extends Closeable> T track(T resource) {
            resources.add(resource);
            return resource;
        }

        private void closeAll(org.simpleframework.http.Response resp) {
            resources.forEach(this::close);
            close(resp);
        }

        private void close(org.simpleframework.http.Response resp) {
            try {
                resp.close();
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