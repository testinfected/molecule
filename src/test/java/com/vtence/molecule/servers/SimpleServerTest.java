package com.vtence.molecule.servers;

import com.vtence.molecule.Server;

public class SimpleServerTest extends ServerCompatibilityTests {

    protected Server createServer(int port) {
        return new SimpleServer("localhost", port);
    }
}
