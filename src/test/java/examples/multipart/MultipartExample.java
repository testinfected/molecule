package examples.multipart;

import com.vtence.molecule.BodyPart;
import com.vtence.molecule.Response;
import com.vtence.molecule.WebServer;
import com.vtence.molecule.lib.TextBody;
import com.vtence.molecule.routing.Routes;

import java.io.IOException;

/**
 * <p>
 * This example shows how to handle multi-part file uploads.
 * </p>
 * <p>
 * We have a first endpoint to serve a very simple profile HTML form in which our user can
 * enter an email address, upload a text biography and a profile image.
 * <br>
 * This form is submitted encoded as <code>multipart/form-data</code>.
 * We process the form submission in a second endpoint.
 * </p>
 */
public class MultipartExample {

    public void run(WebServer server) throws IOException {
        // Start the server with a set of routes
        server.route(new Routes() {{

            // A GET request to /profile renders an HTML profile form. It will be submitted as
            // multipart/form-data. In this form the user can enter an email address, upload a text biography
            // and a profile image
            get("/profile").to(
                    request -> Response.ok()
                                       // Set the content type of the response to text/html
                                       .contentType("text/html; charset=utf-8")
                                       // Render the profile form
                                       .done("<html>" +
                                             "<body>" +
                                             "<form enctype='multipart/form-data' action='/profile' method='post'>\n" +
                                             "<p>" +
                                             "  <label>Email: <input type=\"email\" name=\"email\"></label>\n" +
                                             "</p>" +
                                             "<p>" +
                                             "  <label>Biography: <input type=\"file\" name=\"biography\"></label>\n" +
                                             "</p>" +
                                             "<p>" +
                                             "  <label>Avatar: <input type=\"file\" name=\"avatar\"></label>\n" +
                                             "</p>" +
                                             "<p>" +
                                             "  <input type=\"submit\" value=\"Go\">\n" +
                                             "</p>" +
                                             "</form>" +
                                             "</body>" +
                                             "</html>"));

            // A POST to /profile submits the form, then returns a plain text page with a summary of the
            // profile.
            post("/profile").to(request -> {
                // Get the email address as a body part
                BodyPart email = request.part("email");
                // Get the biography as a second body part
                BodyPart biography = request.part("biography");
                // Get the avatar image as the last body part
                BodyPart avatar = request.part("avatar");

                // We respond with plain text content containing the profile information
                TextBody echo = new TextBody();
                if (email != null)
                    // We read the value of the email part as text
                    echo.append("email: ").append(email.value()).append("\n");
                if (biography != null)
                    // We read the value of the file containing the biography as text
                    echo.append("biography: ").append(biography.value()).append("\n");
                if (avatar != null)
                    echo.append("avatar: ")
                        // We read the filename of the avatar and its content-type
                        .append(avatar.filename()).append(" (").append(avatar.contentType()).append(")")
                        // We also read the content of the image as a raw stream of bytes to calculate its length
                        .append(" - ").append(String.valueOf(avatar.content().length)).append(" bytes");
                return Response.ok()
                               .contentType("text/plain; charset=utf-8")
                               .done(echo);
            });
        }});
    }

    public static void main(String[] args) throws IOException {
        MultipartExample example = new MultipartExample();
        WebServer webServer = WebServer.create();
        example.run(webServer);
        System.out.println("Access at " + webServer.uri() + "/profile");
    }
}
