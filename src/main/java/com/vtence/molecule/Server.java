package com.vtence.molecule;

import java.io.IOException;

public interface Server {

    void run(Application app) throws IOException;

    void shutdown() throws IOException;

    int port();
}
