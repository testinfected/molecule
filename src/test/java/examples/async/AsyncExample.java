package examples.async;

import com.vtence.molecule.WebServer;
import com.vtence.molecule.middlewares.Failsafe;

import java.io.IOException;
import java.util.concurrent.CompletionException;

import static java.util.concurrent.CompletableFuture.runAsync;

public class AsyncExample {

    public void run(WebServer server) throws IOException {
        // Capture internal server errors and display a 500 page
        server.add(new Failsafe());
        server.start((request, response) -> {
            runAsync(() -> {
                aLongRunningProcess(500);

                response.body("After waiting for a long time...")
                        .done();
            });
        });
    }

    private void aLongRunningProcess(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            throw new CompletionException(e);
        }
    }


    public static void main(String[] args) throws IOException {
        AsyncExample example = new AsyncExample();
        // Run the default web server
        WebServer webServer = WebServer.create();
        example.run(webServer);
        System.out.println("Access at " + webServer.uri());
    }
}