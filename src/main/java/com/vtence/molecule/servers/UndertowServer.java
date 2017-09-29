package com.vtence.molecule.servers;

import com.vtence.molecule.Application;
import com.vtence.molecule.Body;
import com.vtence.molecule.BodyPart;
import com.vtence.molecule.FailureReporter;
import com.vtence.molecule.Request;
import com.vtence.molecule.Response;
import com.vtence.molecule.Server;
import com.vtence.molecule.helpers.Headers;
import com.vtence.molecule.http.Uri;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.form.FormData;
import io.undertow.server.handlers.form.FormDataParser;
import io.undertow.server.handlers.form.FormEncodedDataDefinition;
import io.undertow.server.handlers.form.MultiPartParserDefinition;
import io.undertow.util.HeaderValues;
import org.xnio.IoUtils;

import javax.net.ssl.SSLContext;
import java.io.Closeable;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionException;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.vtence.molecule.http.HttpMethod.valueOf;
import static io.undertow.util.HttpString.tryFromString;

public class UndertowServer implements Server {

    private final String host;
    private final int port;

    private Undertow server;
    private FailureReporter failureReporter = FailureReporter.IGNORE;

    public UndertowServer(String host, int port) {
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
        start(Undertow.builder().addHttpListener(port, host), app);
    }

    public void run(final Application app, SSLContext context) throws IOException {
        start(Undertow.builder().addHttpsListener(port, host, context), app);
    }

    private void start(Undertow.Builder builder, Application app) {
        server = builder.setHandler(new DispatchHandler(app))
                        .build();
        server.start();
    }

    public void shutdown() throws IOException {
        if (server != null) server.stop();
    }

    private class DispatchHandler implements HttpHandler {
        private final Application app;

        public DispatchHandler(Application app) {
            this.app = app;
        }

        public void handleRequest(HttpServerExchange exchange) throws Exception {
            exchange.startBlocking();
            exchange.dispatch(() -> new RequestHandler(app).handleRequest(exchange));
        }
    }

    private class RequestHandler implements HttpHandler {
        private final List<Closeable> resources = new ArrayList<>();
        private final Application app;

        public RequestHandler(Application app) {
            this.app = app;
        }

        public void handleRequest(HttpServerExchange exchange) {
            try {
                app.handle(asRequest(exchange))
                   .whenSuccessful(transferTo(exchange))
                   .whenFailed((result, error) -> failureReporter.errorOccurred(error))
                   .whenComplete((result, error) -> closeAll(exchange));
            } catch (Throwable failure) {
                failureReporter.errorOccurred(failure);
                closeAll(exchange);
            }
        }

        private Request asRequest(HttpServerExchange exchange) throws IOException {
            return makeRequest(exchange)
                    .remoteIp(exchange.getSourceAddress().getAddress().getHostAddress())
                    .remotePort(exchange.getSourceAddress().getPort())
                    .remoteHost(exchange.getSourceAddress().getHostName())
                    .timestamp(exchange.getRequestStartTime())
                    .protocol(exchange.getProtocol().toString())
                    .secure(exchange.getConnection().getSslSessionInfo() != null)
                    .body(readBody(exchange));
        }

        private Request makeRequest(HttpServerExchange exchange) throws IOException {
            return new Request(valueOf(exchange.getRequestMethod().toString()),
                               reconstructUri(exchange),
                               readHeaders(exchange),
                               readParameters(exchange),
                               readParts(exchange));
        }

        private Uri reconstructUri(HttpServerExchange exchange) {
            Uri uri = Uri.of(exchange.getRequestURI())
                         .query(exchange.getQueryString());
            if (uri.scheme() == null) uri = uri.scheme(exchange.getRequestScheme());
            if (uri.host() == null) uri = uri.host(host);
            if (uri.port() == -1) uri = uri.port(port);
            return uri;
        }

        private Headers readHeaders(HttpServerExchange exchange) {
            Headers headers = new Headers();
            for (HeaderValues values : exchange.getRequestHeaders()) {
                for (String value : values) {
                    headers.add(values.getHeaderName().toString(), value);
                }
            }
            return headers;
        }

        private Map<String, List<String>> readParameters(HttpServerExchange exchange) throws IOException {
            Map<String, List<String>> parameters = new HashMap<>();
            parameters.putAll(readQueryParameters(exchange));
            parameters.putAll(readFormParameters(exchange));
            return parameters;
        }

        private Map<String, List<String>> readQueryParameters(HttpServerExchange exchange) {
            return exchange.getQueryParameters().entrySet().stream().collect(Collectors.toMap(
                    Map.Entry::getKey, e -> new ArrayList<>(e.getValue())));
        }

        private Map<String, List<String>> readFormParameters(HttpServerExchange exchange) throws IOException {
            FormEncodedDataDefinition form = new FormEncodedDataDefinition();
            form.setDefaultEncoding(StandardCharsets.UTF_8.name());

            try (FormDataParser parser = form.create(exchange)) {
                Map<String, List<String>> parameters = new HashMap<>();
                if (parser == null) return parameters;

                FormData data = parser.parseBlocking();
                for (String name : data) {
                    for (FormData.FormValue param : data.get(name)) {
                        parameters.computeIfAbsent(name, key -> new ArrayList<>())
                                  .add(param.getValue());
                    }
                }
                return parameters;
            }
        }

        private List<BodyPart> readParts(HttpServerExchange exchange) throws IOException {
            MultiPartParserDefinition multipart = new MultiPartParserDefinition();
            multipart.setDefaultEncoding(StandardCharsets.UTF_8.name());

            try(FormDataParser parser = multipart.create(exchange)) {
                List<BodyPart> parts = new ArrayList<>();
                if (parser == null) return parts;

                FormData data = parser.parseBlocking();
                for (String name : data) {
                    for (FormData.FormValue param : data.get(name)) {
                        BodyPart part = new BodyPart().filename(param.getFileName())
                                                      .contentType(contentTypeOf(param))
                                                      .name(name);
                        if (param.isFile()) {
                            final FileInputStream content = new FileInputStream(param.getPath().toFile());
                            part.content(track(content));
                        } else {
                            part.content(param.getValue());
                        }
                        parts.add(part);
                    }
                }

                return parts;
            }
        }

        private String contentTypeOf(FormData.FormValue param) {
            HeaderValues contentType = param.getHeaders().get("Content-Type");
            return contentType != null ? contentType.getFirst() : null;
        }

        private InputStream readBody(HttpServerExchange exchange) throws IOException {
            return track(exchange.getInputStream());
        }

        private Consumer<Response> transferTo(HttpServerExchange exchange) {
            return response -> {
                try {
                    commit(exchange, response);
                } catch (IOException e) {
                    throw new CompletionException(e);
                }
            };
        }

        private void commit(HttpServerExchange exchange, Response response) throws IOException {
            setStatusLine(exchange, response);
            setHeaders(exchange, response);
            writeBody(exchange, response);
        }

        private void setStatusLine(HttpServerExchange exchange, Response response) {
            exchange.setStatusCode(response.statusCode());
            exchange.setReasonPhrase(response.statusDescription());
        }

        private void setHeaders(HttpServerExchange exchange, Response response) {
            response.headerNames().forEach(name -> {
                response.headers(name).forEach(
                        value -> exchange.getResponseHeaders().add(tryFromString(name), value));
            });
        }

        private void writeBody(HttpServerExchange exchange, Response response) throws IOException {
            try (Body body = response.body()) {
                body.writeTo(exchange.getOutputStream(), response.charset());
            }
        }

        private <T extends Closeable> T track(T resource) {
            resources.add(resource);
            return resource;
        }

        private void closeAll(HttpServerExchange exchange) {
            resources.forEach(this::close);
            end(exchange);
        }

        private void close(Closeable resource) {
            try {
                resource.close();
            } catch (IOException e) {
                failureReporter.errorOccurred(e);
            }
        }

        private void end(HttpServerExchange exchange) {
            try {
                exchange.endExchange();
            } catch (Throwable t) {
                failureReporter.errorOccurred(t);
                IoUtils.safeClose(exchange.getConnection());
            }
        }
    }
}