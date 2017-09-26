package com.vtence.molecule;

public interface Middleware {

    Application then(Application next);

    // Support applications as functions with this trick until Application is a pure function
    default Application then(Application.ApplicationFunction application) {
        return then(Application.of(application));
    }

    /**
     * Compose this middleware with the next in chain.
     */
    default Middleware compose(Middleware next) {
        return application -> then(next.then(application));
    }

    /**
     * The identity function.
     */
    static Middleware identity() {
        return application -> application;
    }
}
