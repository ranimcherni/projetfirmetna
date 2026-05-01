package services;

import org.json.JSONArray;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class TranslationService {

    private static final HttpClient client = HttpClient.newHttpClient();

    public static String translate(String text, String targetLangObj) {
        if (text == null || text.trim().isEmpty()) {
            return text;
        }

        // Map language name to ISO code
        String targetLang = "fr";
        if (targetLangObj != null) {
            switch (targetLangObj.toLowerCase()) {
                case "anglais": targetLang = "en"; break;
                case "arabe": targetLang = "ar"; break;
                case "espagnol": targetLang = "es"; break;
                case "français": 
                case "francais":
                default: targetLang = "fr"; break;
            }
        }

        try {
            String encodedText = URLEncoder.encode(text, StandardCharsets.UTF_8.toString());
            String url = "https://translate.googleapis.com/translate_a/single?client=gtx&sl=auto&tl=" 
                        + targetLang + "&dt=t&q=" + encodedText;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JSONArray jsonArray = new JSONArray(response.body());
                JSONArray translatedItems = jsonArray.getJSONArray(0);
                StringBuilder translatedText = new StringBuilder();

                for (int i = 0; i < translatedItems.length(); i++) {
                    JSONArray item = translatedItems.getJSONArray(i);
                    translatedText.append(item.getString(0));
                }

                return translatedText.toString();
            }
        } catch (Exception e) {
            System.err.println("Translation Error: " + e.getMessage());
        }

        return text; // Return original on error
    }
}
