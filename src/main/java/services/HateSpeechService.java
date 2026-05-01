package services;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * Calls the Google Perspective API to detect hate speech / toxicity.
 * Attributes checked: TOXICITY, IDENTITY_ATTACK, INSULT, THREAT, PROFANITY.
 * Returns true (toxic) if any attribute score exceeds the configured threshold.
 */
public class HateSpeechService {

    private static final String ENDPOINT =
            "https://commentanalyzer.googleapis.com/v1alpha1/comments:analyze?key=";

    private final String apiKey;
    private final double threshold;

    public HateSpeechService() {
        Properties props = new Properties();
        try (InputStream in = getClass().getClassLoader()
                .getResourceAsStream("config.properties")) {
            if (in != null) {
                props.load(in);
            }
        } catch (IOException e) {
            System.err.println("HateSpeechService: could not load config.properties");
        }
        this.apiKey    = props.getProperty("perspective.api.key", "");
        this.threshold = Double.parseDouble(
                props.getProperty("perspective.threshold", "0.70"));
    }

    /**
     * Result container returned to the caller.
     */
    public static class Result {
        public final boolean toxic;
        public final double  score;
        public final String  reason;   // which attribute triggered, e.g. "TOXICITY"

        Result(boolean toxic, double score, String reason) {
            this.toxic  = toxic;
            this.score  = score;
            this.reason = reason;
        }
    }

    /**
     * Analyse {@code text} for hate speech.
     *
     * @param text the content to analyse (titre + contenu, or comment body)
     * @return a {@link Result}; if the API is unreachable returns toxic=false so
     *         the app never blocks users due to a network error.
     */
    public Result analyze(String text) {
        if (apiKey == null || apiKey.isBlank()) {
            System.err.println("HateSpeechService: API key not configured – skipping check.");
            return new Result(false, 0.0, "");
        }

        // Truncate to 3000 chars (API limit is 20 000 bytes, but keep it short)
        if (text.length() > 3000) text = text.substring(0, 3000);

        String json = buildRequestJson(text);

        try {
            HttpURLConnection conn = (HttpURLConnection)
                    new URL(ENDPOINT + apiKey).openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setDoOutput(true);
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(8000);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(json.getBytes(StandardCharsets.UTF_8));
            }

            int status = conn.getResponseCode();
            if (status != 200) {
                System.err.println("HateSpeechService: HTTP " + status + " from Perspective API");
                return new Result(false, 0.0, "");
            }

            String response = readStream(conn.getInputStream());
            return parseResponse(response);

        } catch (Exception e) {
            System.err.println("HateSpeechService: " + e.getMessage());
            // Fail open — don't block users when network is unavailable
            return new Result(false, 0.0, "");
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private static String buildRequestJson(String text) {
        // Escape the text for JSON
        String escaped = text
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");

        return "{"
                + "\"comment\":{\"text\":\"" + escaped + "\"},"
                + "\"languages\":[\"fr\",\"en\",\"ar\"],"
                + "\"requestedAttributes\":{"
                + "\"TOXICITY\":{},"
                + "\"IDENTITY_ATTACK\":{},"
                + "\"INSULT\":{},"
                + "\"THREAT\":{},"
                + "\"PROFANITY\":{}"
                + "}"
                + "}";
    }

    /**
     * Minimal JSON parser — extracts the summaryScore value for each attribute
     * without requiring an external JSON library.
     */
    private Result parseResponse(String response) {
        String[] attributes = {"TOXICITY", "IDENTITY_ATTACK", "INSULT", "THREAT", "PROFANITY"};

        double maxScore     = 0.0;
        String triggerAttr  = "";

        for (String attr : attributes) {
            // Find the block for this attribute, then locate "summaryScore"
            int attrIdx = response.indexOf("\"" + attr + "\"");
            if (attrIdx == -1) continue;

            int ssIdx = response.indexOf("\"summaryScore\"", attrIdx);
            if (ssIdx == -1) continue;

            int valueIdx = response.indexOf("\"value\"", ssIdx);
            if (valueIdx == -1) continue;

            int colon = response.indexOf(":", valueIdx);
            if (colon == -1) continue;

            int comma = response.indexOf(",", colon);
            int brace = response.indexOf("}", colon);
            int end   = (comma != -1 && comma < brace) ? comma : brace;

            String valueStr = response.substring(colon + 1, end).trim();
            try {
                double score = Double.parseDouble(valueStr);
                if (score > maxScore) {
                    maxScore     = score;
                    triggerAttr  = attr;
                }
            } catch (NumberFormatException ignored) {}
        }

        boolean toxic = maxScore >= threshold;
        return new Result(toxic, maxScore, triggerAttr);
    }

    private static String readStream(InputStream is) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
        }
        return sb.toString();
    }
}
