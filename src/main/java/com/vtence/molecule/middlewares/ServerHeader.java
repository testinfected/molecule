package com.vtence.molecule.middlewares;

import com.vtence.molecule.Request;
import com.vtence.molecule.Response;

import static com.vtence.molecule.HttpHeaders.SERVER;

public class ServerHeader extends AbstractMiddleware {

    private final String serverName;

    public ServerHeader(String serverName) {
        this.serverName = serverName;
    }

    public void handle(Request request, Response response) throws Exception {
        response.set(SERVER, serverName);

        forward(request, response);
    }
}
