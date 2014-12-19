package com.vtence.molecule.servers;

import com.vtence.molecule.Application;
import com.vtence.molecule.Body;
import com.vtence.molecule.http.Cookie;
import com.vtence.molecule.Request;
import com.vtence.molecule.Response;
import com.vtence.molecule.Server;
import com.vtence.molecule.FailureReporter;
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

        public void handle(org.simpleframework.http.Request httpRequest, org.simpleframework.http.Response httpResponse) {
            try {
                Request request = new Request();
                Response response = new Response();
                build(request, httpRequest);
                app.handle(request, response);
                commit(httpResponse, response);
            } catch (Throwable failure) {
                failureReporter.errorOccurred(failure);
            } finally {
                close(httpResponse);
            }
        }

        private void build(Request request, org.simpleframework.http.Request httpRequest) throws IOException {
            request.uri(httpRequest.getTarget());
            request.path(httpRequest.getPath().getPath());
            request.remoteIp(httpRequest.getClientAddress().getAddress().getHostAddress());
            request.remotePort(httpRequest.getClientAddress().getPort());
            request.remoteHost(httpRequest.getClientAddress().getHostName());
            request.timestamp(httpRequest.getRequestTime());
            request.protocol(String.format("HTTP/%s.%s", httpRequest.getMajor(), httpRequest.getMinor()));
            request.secure(httpRequest.isSecure());
            request.body(httpRequest.getInputStream());
            request.method(httpRequest.getMethod());
            setHeaders(request, httpRequest);
            setCookies(request, httpRequest);
            setParameters(request, httpRequest);
        }

        private void setHeaders(Request request, org.simpleframework.http.Request httpRequest) {
            List<String> names = httpRequest.getNames();
            for (String header : names) {
                // Values returned like this are stripped of the quality values ...
                int headerCount = httpRequest.getValues(header).size();
                // ... so we get them by index instead
                for (int index = 0; index < headerCount; index++) {
                    request.addHeader(header, httpRequest.getValue(header, index));
                }
            }
        }

        private void setCookies(Request request, org.simpleframework.http.Request httpRequest) {
            for (org.simpleframework.http.Cookie cookie : httpRequest.getCookies()) {
                request.cookie(cookie.getName(), cookie.getValue());
            }
        }

        private void setParameters(Request request, org.simpleframework.http.Request httpRequest) {
            for (String name : httpRequest.getQuery().keySet()) {
                List<String> values = httpRequest.getQuery().getAll(name);
                for (String value : values) {
                    request.addParameter(name, value);
                }
            }
        }

        private void commit(org.simpleframework.http.Response httpResponse, Response response) throws IOException {
            httpResponse.setCode(response.statusCode());
            httpResponse.setDescription(response.statusText());
            commitHeaders(httpResponse, response);
            commitCookies(httpResponse, response);
            Body body = response.body();
            body.writeTo(httpResponse.getOutputStream(), response.charset());
            body.close();
        }

        private void commitHeaders(org.simpleframework.http.Response httpResponse, Response response) {
            for (String name : response.names()) {
                httpResponse.setValue(name, response.get(name));
            }
        }

        private void commitCookies(org.simpleframework.http.Response httpResponse, Response response) {
            for (Cookie cookie : response.cookies()) {
                org.simpleframework.http.Cookie httpCookie = httpResponse.setCookie(cookie.name(), cookie.value());
                httpCookie.setExpiry(cookie.maxAge());
                httpCookie.setDomain(cookie.domain());
                httpCookie.setPath(cookie.path());
                httpCookie.setSecure(cookie.secure());
                httpCookie.setProtected(cookie.httpOnly());
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