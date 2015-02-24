package examples.multipart;

import com.vtence.molecule.Application;
import com.vtence.molecule.BodyPart;
import com.vtence.molecule.Request;
import com.vtence.molecule.Response;
import com.vtence.molecule.WebServer;
import com.vtence.molecule.lib.TextBody;
import com.vtence.molecule.routing.DynamicRoutes;

import java.io.IOException;

public class MultipartExample {

    public void run(WebServer server) throws IOException {
        server.start(new DynamicRoutes() {{
            get("/profile").to(new Application() {
                public void handle(Request request, Response response) throws Exception {
                    response.contentType("text/html");
                    response.body("<html>" +
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
                            "</html>");
                }
            });

            post("/profile").to(new Application() {
                public void handle(Request request, Response response) throws Exception {
                    BodyPart email = request.part("email");
                    BodyPart biography = request.part("biography");
                    BodyPart avatar = request.part("avatar");

                    response.contentType("text/plain");
                    TextBody echo = new TextBody();
                    if (email != null)
                        echo.append("email: ").append(email.value()).append("\n");
                    if (biography != null)
                        echo.append("biography: ").append(biography.value()).append("\n");
                    if (avatar != null)
                        echo.append("avatar: ")
                            .append(avatar.filename()).append(" (").append(avatar.contentType()).append(")")
                            .append(" - ").append(String.valueOf(avatar.content().length)).append(" bytes");
                    response.body(echo);
                }
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