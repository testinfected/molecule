package com.vtence.molecule;

public interface Middleware extends Application {

    // Define a functional interface ...
    interface MiddlewareFunction {
        Application then(Application next);
    }

    // ... so that we can use method references with new our style `Middleware`s ...
    static Middleware from(MiddlewareFunction middleware) {
        return new Middleware() {
            public void handle(Request request, Response response) throws Exception {
                throw new UnsupportedOperationException();
            }

            public void connectTo(Application successor) {
                throw new UnsupportedOperationException();
            }

            public Application then(Application application) {
                return middleware.then(application);
            }
        };
    }

    // .. until eventually this becomes our new functional interface ...
    default Application then(Application next) {
        connectTo(next);
        return this;
    }

    // ... and this is gone.
    void connectTo(Application successor);

    // Until then support application as functions with this trick
    default Application then(ApplicationFunction application) {
        return then(Application.of(application));
    }

    /**
     * Returns a composed middleware that chains this middleware
     * with the next.
     */
    default Middleware then(Middleware next) {
        return new Middleware() {
            public void handle(Request request, Response response) throws Exception {
                throw new UnsupportedOperationException();
            }

            public void connectTo(Application successor) {
                throw new UnsupportedOperationException();
            }

            public Application then(Application application) {
                return Middleware.this.then(next.then(application));
            }
        };
    }

    static Middleware identity() {
        return Middleware.from(application -> application);
    }
}
