package examples.simple;

import com.vtence.molecule.WebServer;
import com.vtence.molecule.middlewares.Failsafe;

import java.io.IOException;
import java.nio.charset.Charset;

public class SimpleExample {

    public void run(WebServer server) throws IOException {
        // Capture internal server errors and display a 500 page
        server.add(new Failsafe());
        server.start((request, response) -> {
            // An unsupported charset will cause an exception, which will cause the FailSafe middleware
            // to render a 500 page
            Charset encoding = Charset.forName(request.parameter("encoding"));
            // The specified charset will be used automatically to encode the response
            response.contentType("text/html; charset=" + encoding.displayName().toLowerCase());

            response.body(
                    "<html>" +
                        "<body>" +
                            "<p>" +
                            "Les naïfs ægithales hâtifs pondant à Noël où il gèle sont sûrs " +
                            "d'être déçus en voyant leurs drôles d'œufs abîmés." +
                            "</p>" +
                        "</body>" +
                    "</html>"
            );
        });
    }


    public static void main(String[] args) throws IOException {
        SimpleExample example = new SimpleExample();
        // Run the default web server
        WebServer webServer = WebServer.create();
        example.run(webServer);
        System.out.println("Access at " + webServer.uri() + "?encoding=utf-8");
    }
}