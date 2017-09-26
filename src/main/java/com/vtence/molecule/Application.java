package com.vtence.molecule;

@FunctionalInterface
public interface Application {

    // ... so that we can use method references with new our style `Application`s ...
    static Application of(Application application) {
        return application;
    }

    Response handle(Request request) throws Exception;
}