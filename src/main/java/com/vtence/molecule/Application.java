package com.vtence.molecule;

@FunctionalInterface
public interface Application {

    // Define a functional interface ...
    interface ApplicationFunction {
        Response handle(Request request) throws Exception;
    }

    // ... so that we can use method references with new our style `Application`s ...
    static Application of(ApplicationFunction application) {
        return new Application() {
            public void handle(Request request, Response response) throws Exception {
            }

            public Response handle(Request request) throws Exception {
                return application.handle(request);
            }
        };
    }

    // ... until we get rid of this signature ...
    void handle(Request request, Response response) throws Exception;

    // ... and make this one the function
    default Response handle(Request request) throws Exception {
        Response response = new Response();
        handle(request, response);
        return response;
    }
}