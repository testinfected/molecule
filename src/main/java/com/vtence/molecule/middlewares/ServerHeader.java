package com.vtence.molecule.middlewares;

import com.vtence.molecule.Application;
import com.vtence.molecule.Middleware;
import com.vtence.molecule.Response;

import static com.vtence.molecule.http.HeaderNames.SERVER;

public class ServerHeader implements Middleware {

    private final String serverName;

    public ServerHeader(String serverName) {
        this.serverName = serverName;
    }

    public Application then(Application next) {
        return Application.of(request -> next.handle(request)
                                             .whenSuccessful(this::setServerHeaderIfMissing));
    }

    private void setServerHeaderIfMissing(Response response) {
        if (!response.hasHeader(SERVER)) {
            response.header(SERVER, serverName);
        }
    }
}