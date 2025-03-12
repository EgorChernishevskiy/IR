import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class GuitarClassifier {
    private final String serviceUrl;

    public GuitarClassifier(String serviceUrl) {
        this.serviceUrl = serviceUrl;
    }

    public String predict(String query) {
        try {
            String encodedQuery = URLEncoder.encode(query, "UTF-8");
            URL url = new URL(serviceUrl + "?query=" + encodedQuery);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream(), "UTF-8")
            );
            String response = in.readLine();
            in.close();
            return response;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "electric";
    }
}
