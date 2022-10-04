import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpClient.Version;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;

/**
 * Class representing the Maven central repo, able to search the Maven central repository using the
 * provided REST api.
 * 
 * @author Cameron Perdue
 * created on 09/29/2022
 */

public class MavenRepo {

    public void searchMaven() {
        HttpClient client = null;
        HttpRequest request = null;
        
        try {
            client = HttpClient.newBuilder()
                .version(Version.HTTP_1_1)
                .followRedirects(Redirect.NORMAL)
                .connectTimeout(Duration.ofSeconds(20))
                .build();
            
            request = HttpRequest.newBuilder()
                .uri(URI.create("https://search.maven.org/solrsearch/select?q=guice&rows=20&wt=xml"))
                .build();

            HttpResponse<String> response = client.send(request, BodyHandlers.ofString());

            System.out.println(response.body());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } 
        finally { }
    }
}
