package com.vtence.molecule.middlewares;

import com.vtence.molecule.Application;
import com.vtence.molecule.Middleware;
import com.vtence.molecule.Request;
import com.vtence.molecule.Response;

public abstract class AbstractMiddleware implements Middleware {

    protected Application successor = new Application() {
        public void handle(Request request, Response response) throws Exception {
        }
    };

    public void connectTo(Application successor) {
        this.successor = successor;
    }

    protected void forward(Request request, Response response) throws Exception {
        successor.handle(request, response);
    }
}
