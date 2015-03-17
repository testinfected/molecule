package examples.cookies;

import com.vtence.molecule.WebServer;
import com.vtence.molecule.testing.http.HttpRequest;
import com.vtence.molecule.testing.http.HttpResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static com.vtence.molecule.testing.http.HttpResponseAssert.assertThat;

public class CookiesTest {

    CookiesExample example = new CookiesExample();
    WebServer server = WebServer.create(9999);

    HttpRequest request = new HttpRequest(9999);
    HttpResponse response;

    @Before
    public void startServer() throws IOException {
        example.run(server);
    }

    @After
    public void stopServer() throws IOException {
        server.stop();
    }

    @Test
    public void readingClientCookies() throws IOException {
        response = request.cookie("customer", "Wile E. Coyote").get("/");
        assertThat(response).hasBodyText("Welcome, Wile E. Coyote");
    }

    @Test
    public void sendingBackANewCookie() throws IOException {
        response = request.get("/weapon");
        assertThat(response).hasCookie("weapon").hasValue("rocket launcher").hasPath("/ammo");

        response = request.cookie("weapon", "rocket launcher").get("/ammo");
        assertThat(response).hasCookie("ammo").hasValue("riding rocket").hasMaxAge(30);
    }

    @Test
    public void refreshingACookie() throws IOException {
        response = request.cookie("weapon", "rocket launcher").cookie("ammo", "riding rocket").get("/ammo");
        assertThat(response).hasCookie("ammo").hasValue("riding rocket").hasPath("/ammo").hasMaxAge(30);
    }

    @Test
    public void expiringACookie() throws IOException {
        response = request.cookie("weapon", "rocket launcher").get("/backfire");
        assertThat(response).hasCookie("weapon").hasPath("/ammo").hasMaxAge(0);
    }
}