package com.vtence.molecule;

public interface Middleware {

    Application then(Application next);

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
