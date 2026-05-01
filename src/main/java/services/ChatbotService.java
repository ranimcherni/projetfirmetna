package services;

import java.io.IOException;
import java.net.ProxySelector;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class ChatbotService {

    private static final String API_URL = "https://text.pollinations.ai/";
    private static final Duration TIMEOUT = Duration.ofSeconds(60);

    private final HttpClient httpClient;

    public ChatbotService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(TIMEOUT)
                .proxy(ProxySelector.getDefault())
                .build();
    }

    public String sendMessage(String userMessage) {
        if (userMessage == null || userMessage.isBlank()) {
            return "Veuillez entrer un message.";
        }

        try {
            // Contexte Firmetna + message utilisateur
            String prompt = buildPrompt(userMessage.trim());
            String encodedPrompt = URLEncoder.encode(prompt, StandardCharsets.UTF_8);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL + encodedPrompt))
                    .timeout(TIMEOUT)
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)
            );

            if (response.statusCode() == 200) {
                String result = response.body();
                if (result != null && !result.isBlank()) {
                    return result.trim();
                }
                return "L'assistant n'a pas pu générer de réponse.";
            } else {
                return "Erreur API (" + response.statusCode() + "). Réessayez.";
            }

        } catch (java.net.http.HttpTimeoutException e) {
            return "Délai dépassé. Veuillez réessayer.";
        } catch (java.net.ConnectException e) {
            return "Connexion impossible. Vérifiez votre connexion internet.";
        } catch (IOException | InterruptedException e) {
            return "Erreur technique : " + e.getMessage();
        } catch (Exception e) {
            return "Erreur inattendue : " + e.getMessage();
        }
    }

    private String buildPrompt(String userMessage) {
        return "Tu es l'assistant intelligent de Firmetna, une application tunisienne " +
               "de gestion de dons et d'événements agricoles. " +
               "Réponds toujours en français, de façon claire et concise. " +
               "Question de l'utilisateur : " + userMessage;
    }
}
