package test;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class WeatherTest {
    public static void main(String[] args) {
        try {
            URL url = new URL("https://api.open-meteo.com/v1/forecast?latitude=36.8065&longitude=10.1815&current_weather=true");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();
            System.out.println("API Response: " + response.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
