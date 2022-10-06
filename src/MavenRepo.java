import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Class representing the Maven central repo, able to search the Maven central
 * repository using the
 * provided REST api.
 * 
 * @author Cameron Perdue
 *         created on 09/29/2022
 */

public class MavenRepo {

    public void searchMaven() {
        URL serverAddress = null;
        HttpURLConnection connection = null;
        BufferedReader reader = null;
        StringBuilder sb = null;
        String line = null;

        try {
            serverAddress = new URL("https://search.maven.org/solrsearch/select?q=guice&rows=20&wt=xml");
            connection = null;

            connection = (HttpURLConnection) serverAddress.openConnection();
            connection.setRequestMethod("GET");
            connection.setDoOutput(true);
            connection.setReadTimeout(1000);

            connection.connect();

            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            sb = new StringBuilder();

            while ((line = reader.readLine()) != null) {
                sb.append(line + '\n');
            }

            System.out.println(sb.toString());

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            connection.disconnect();
            reader = null;
            sb = null;
            connection = null;
        }
    }

}
