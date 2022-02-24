package com.vtence.molecule;

import javax.net.ssl.SSLContext;
import java.io.IOException;

public interface Server {

    void run(Application app, ServerOption... options) throws IOException;

    void run(Application app, SSLContext context, ServerOption... options) throws IOException;

    void shutdown() throws IOException;

    void reportErrorsTo(FailureReporter reporter);

    String host();

    int port();
}