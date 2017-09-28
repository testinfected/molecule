package com.vtence.molecule.servers;

import com.vtence.molecule.Application;
import com.vtence.molecule.Body;
import com.vtence.molecule.BodyPart;
import com.vtence.molecule.FailureReporter;
import com.vtence.molecule.Request;
import com.vtence.molecule.Response;
import com.vtence.molecule.Server;
import com.vtence.molecule.http.Uri;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.form.FormData;
import io.undertow.server.handlers.form.FormDataParser;
import io.undertow.server.handlers.form.FormEncodedDataDefinition;
import io.undertow.server.handlers.form.MultiPartParserDefinition;
import io.undertow.util.HeaderMap;
import io.undertow.util.HeaderValues;
import io.undertow.util.HttpString;
import org.xnio.IoUtils;

import javax.net.ssl.SSLContext;
import java.io.Closeable;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionException;
import java.util.function.Consumer;

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
        server = builder.setHandler(new DispatchHandler(new ApplicationHandler(app)))
                        .build();
        server.start();
    }

    public void shutdown() throws IOException {
        if (server != null) server.stop();
    }

    private static class DispatchHandler implements HttpHandler {
        private final ApplicationHandler handler;

        public DispatchHandler(ApplicationHandler handler) {
            this.handler = handler;
        }

        public void handleRequest(HttpServerExchange exchange) throws Exception {
            exchange.startBlocking();
            exchange.dispatch(() -> handler.handleRequest(exchange));
        }
    }

    private class ApplicationHandler implements HttpHandler {
        private final Application app;

        public ApplicationHandler(Application app) {
            this.app = app;
        }

        public void handleRequest(HttpServerExchange exchange) {
            final List<Closeable> resources = new ArrayList<>();
            try {
                Request request = asRequest(exchange, resources);
                app.handle(request)
                   .whenSuccessful(transferTo(exchange))
                   .whenFailed((result, error) -> failureReporter.errorOccurred(error))
                   .whenComplete((result, error) -> closeAll(resources, exchange));
            } catch (Throwable failure) {
                failureReporter.errorOccurred(failure);
                closeAll(resources, exchange);
            }
        }

        private Request asRequest(HttpServerExchange exchange, List<Closeable> resources) throws IOException {
            Request request = read(exchange);
            setHeaders(request, exchange);
            setQueryParameters(request, exchange);
            setFormParameters(request, exchange);
            setParts(request, exchange, resources);
            setBody(request, exchange, resources);
            return request;
        }

        private Request read(HttpServerExchange exchange) {
            return new Request(exchange.getRequestMethod().toString(), reconstructUri(exchange))
                    .remoteIp(exchange.getSourceAddress().getAddress().getHostAddress())
                    .remotePort(exchange.getSourceAddress().getPort())
                    .remoteHost(exchange.getSourceAddress().getHostName())
                    .timestamp(exchange.getRequestStartTime())
                    .protocol(exchange.getProtocol().toString())
                    .secure(exchange.getConnection().getSslSessionInfo() != null);
        }

        private Uri reconstructUri(HttpServerExchange exchange) {
            Uri uri = Uri.of(exchange.getRequestURI())
                         .query(exchange.getQueryString());
            if (uri.scheme() == null) uri = uri.scheme(exchange.getRequestScheme());
            if (uri.host() == null) uri = uri.host(host);
            if (uri.port() == -1) uri = uri.port(port);
            return uri;
        }

        private void setHeaders(Request request, HttpServerExchange exchange) {
            HeaderMap headers = exchange.getRequestHeaders();
            for (HeaderValues values : headers) {
                for (String value : values) {
                    request.addHeader(values.getHeaderName().toString(), value);
                }
            }
        }

        private void setQueryParameters(Request request, HttpServerExchange exchange) {
            Map<String, Deque<String>> parameters = exchange.getQueryParameters();
            for (String name : parameters.keySet()) {
                for (String value : parameters.get(name)) {
                    request.addParameter(name, value);
                }
            }
        }

        private void setFormParameters(Request request, HttpServerExchange exchange) throws IOException {
            FormEncodedDataDefinition form = new FormEncodedDataDefinition();
            form.setDefaultEncoding(StandardCharsets.UTF_8.name());
            FormDataParser parser = form.create(exchange);
            if (parser == null) return;

            FormData data = parser.parseBlocking();
            for (String name : data) {
                for (FormData.FormValue param : data.get(name)) {
                    request.addParameter(name, param.getValue());
                }
            }
            parser.close();
        }

        private void setParts(Request request, HttpServerExchange exchange, List<Closeable> resources)
                throws IOException {
            MultiPartParserDefinition multipart = new MultiPartParserDefinition();
            multipart.setDefaultEncoding(StandardCharsets.UTF_8.name());
            FormDataParser parser = multipart.create(exchange);
            if (parser == null) return;

            FormData data = parser.parseBlocking();
            for (String name : data) {
                for (FormData.FormValue param : data.get(name)) {
                    BodyPart part = new BodyPart().filename(param.getFileName())
                                                  .contentType(contentTypeOf(param))
                                                  .name(name);
                    if (param.isFile()) {
                        final FileInputStream content = new FileInputStream(param.getPath().toFile());
                        resources.add(content);
                        part.content(content);
                    } else {
                        part.content(param.getValue());
                    }
                    request.addPart(part);
                }
            }
            parser.close();
        }

        private String contentTypeOf(FormData.FormValue param) {
            HeaderValues contentType = param.getHeaders().get("Content-Type");
            return contentType != null ? contentType.getFirst() : null;
        }

        private void setBody(Request request, HttpServerExchange exchange, List<Closeable> resources)
                throws IOException {
            final InputStream body = exchange.getInputStream();
            resources.add(body);
            request.body(body);
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
            for (String name : response.headerNames()) {
                for (String value : response.headers(name)) {
                    exchange.getResponseHeaders().add(HttpString.tryFromString(name), value);
                }
            }
        }

        private void writeBody(HttpServerExchange exchange, Response response) throws IOException {
            Body body = response.body();
            body.writeTo(exchange.getOutputStream(), response.charset());
            body.close();
        }

        private void closeAll(List<Closeable> resources, HttpServerExchange exchange) {
            for (Closeable resource : resources) {
                close(resource);
            }
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
            } catch (Throwable e) {
                failureReporter.errorOccurred(e);
                IoUtils.safeClose(exchange.getConnection());
            }
        }
    }
}