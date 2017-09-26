package examples.simple;

import com.vtence.molecule.Application;
import com.vtence.molecule.Response;
import com.vtence.molecule.WebServer;
import com.vtence.molecule.middlewares.Failsafe;

import java.io.IOException;
import java.nio.charset.Charset;

/**
 * <p>
 * In this example we run a single application that responds to all incoming requests with an HTML page,
 * varying the encoding.
 * </p>
 * <p>
 * We let the user change the output encoding by specifying the desired charset as a request parameter.
 * </p>
 * <p>
 * There's always the chance that we are thrown an encoding we don't support, so we use the failsafe middleware
 * to turn internal server errors into a 500 error page.
 * </p>
 */
public class SimpleExample {

    public void run(WebServer server) throws IOException {
        // The failsafe middleware captures internal server errors and renders a default 500 page,
        // showing a stack trace of the exception and its causes.
        server.add(new Failsafe())
              .start(Application.of(request -> {
                  // An unsupported charset will cause an exception, which will in turn cause the failsafe middleware
                  // to render a 500 page
                  Charset encoding = Charset.forName(request.parameter("encoding"));
                  return Response.ok()
                                 // The content-type charset will be used automatically to encode the response
                                 .contentType("text/html; charset=" + encoding.displayName().toLowerCase())
                                 // Our HTML page contains characters outside the ISO-8859-1 alphabet.
                                 .done("<html>" +
                                       "<body>" +
                                       "<p>" +
                                       "Les naïfs ægithales hâtifs pondant à Noël où il gèle sont sûrs " +
                                       "d'être déçus en voyant leurs drôles d'œufs abîmés." +
                                       "</p>" +
                                       "</body>" +
                                       "</html>");
              }));
    }


    public static void main(String[] args) throws IOException {
        SimpleExample example = new SimpleExample();
        // Run the default web server
        WebServer webServer = WebServer.create();
        example.run(webServer);
        System.out.println("Access at " + webServer.uri() + "?encoding=utf-8");
    }
}
