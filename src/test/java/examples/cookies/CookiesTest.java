package examples.cookies;

import com.vtence.molecule.WebServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;

import static com.vtence.molecule.testing.http.HttpResponseAssert.assertThat;
import static java.net.http.HttpResponse.BodyHandlers.ofString;

public class CookiesTest {

    CookiesExample example = new CookiesExample();
    WebServer server = WebServer.create(9999);

    CookieManager cookies = new CookieManager();
    HttpClient client = HttpClient.newBuilder().cookieHandler(cookies).build();

    HttpRequest.Builder request = HttpRequest.newBuilder(server.uri());

    @Before
    public void startServer() throws IOException {
        example.run(server);
    }

    @After
    public void stopServer() throws IOException {
        server.stop();
    }

    @Test
    public void readingClientCookies() throws Exception {
        var customer = new HttpCookie("customer", "Wile E. Coyote");
        customer.setPath("/");
        cookies.getCookieStore().add(server.uri(), customer);

        var response = client.send(request.GET().build(), ofString());
        assertThat(response).hasBody("Welcome, Wile E. Coyote");
    }

    @Test
    public void sendingBackANewCookie() throws Exception {
        request.uri(server.uri().resolve("/weapon"));
        var initial = client.send(request.GET().build(), ofString());
        assertThat(initial).hasCookie("weapon")
                            .hasValue("rocket launcher")
                            .hasPath("/ammo");

        request.uri(server.uri().resolve("/ammo"));
        var next = client.send(request.GET().build(), ofString());
        assertThat(next).hasCookie("ammo")
                            .hasValue("riding rocket")
                            .hasMaxAge(30);
    }

    @Test
    public void refreshingACookie() throws Exception {
        var weapon = new HttpCookie("weapon", "rocket launcher");
        weapon.setPath("/ammo");
        cookies.getCookieStore().add(server.uri(), weapon);
        var ammo = new HttpCookie("ammo", "riding rocket");
        weapon.setPath("/ammo");
        cookies.getCookieStore().add(server.uri(), ammo);

        request.uri(server.uri().resolve("/ammo"));
        var response = client.send(request.GET().build(), ofString());
        assertThat(response).hasCookie("ammo")
                            .hasValue("riding rocket")
                            .hasPath("/ammo")
                            .hasMaxAge(30);
    }

    @Test
    public void expiringACookie() throws Exception {
        var weapon = new HttpCookie("weapon", "rocket launcher");
        weapon.setPath("/");
        cookies.getCookieStore().add(server.uri(), weapon);

        request.uri(server.uri().resolve("/backfire"));
        var response = client.send(request.GET().build(), ofString());
        assertThat(response).hasCookie("weapon")
                            .hasPath("/ammo")
                            .hasMaxAge(0);
    }
}