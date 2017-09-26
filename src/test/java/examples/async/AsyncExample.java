package examples.async;

import com.vtence.molecule.Response;
import com.vtence.molecule.WebServer;

import java.io.IOException;
import java.util.concurrent.CompletionException;

import static java.util.concurrent.CompletableFuture.runAsync;

/**
 * <p>
 *     In this example we serve responses from a different thread than the request servicing thread.
 *     We need to be careful not to block the request servicing thread if request processing takes some time to complete.
 *     In this case it is better to run the time consuming process asynchronously and write the response when this
 *     process completes.
 * </p>
 * <p>
 *     This could happen for instance if we are dependent on some remote server to complete a task, or some message
 *     to arrive. Below is a very simple and rather contrived example of how this can be implemented.
 * </p>
 */
public class AsyncExample {

    public void run(WebServer server) throws IOException {
        server.start(request -> {
            Response response = Response.ok();
            // We can serve responses asynchronously from a separate thread, without blocking I/O. We'll use
            // the common fork-join pool to run a task that takes 500ms to complete.
            runAsync(() -> {
                // To simulate a long running process...
                aLongRunningProcess(500);

                // When the task completes, a call to done triggers completion of the response.
                // Processing of the middleware pipeline resumes and then the server writes the response back
                // to the client.
                response.done("After waiting for a long time...");
            });

            return response;
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
