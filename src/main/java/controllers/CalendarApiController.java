package controllers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class CalendarApiController {
    private final DonationController donationController;

    public CalendarApiController() {
        this.donationController = new DonationController();
    }

    /**
     * API simple: GET /calendar/recuperations
     */
    public void startServer(int port) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/calendar/recuperations", new RecuperationsHandler());
        server.setExecutor(null);
        server.start();
        System.out.println("Calendar API started on http://localhost:" + port + "/calendar/recuperations");
    }

    private class RecuperationsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }

            List<DonationController.CalendarEventDto> events = donationController.getCalendarRecuperations();
            String json = toJson(events);
            byte[] payload = json.getBytes(StandardCharsets.UTF_8);

            exchange.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8");
            exchange.sendResponseHeaders(200, payload.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(payload);
            }
        }
    }

    private String toJson(List<DonationController.CalendarEventDto> events) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < events.size(); i++) {
            DonationController.CalendarEventDto e = events.get(i);
            sb.append("{")
                    .append("\"id\":").append(e.getId()).append(",")
                    .append("\"titre\":\"").append(escapeJson(e.getTitre())).append("\",")
                    .append("\"dateRecuperation\":")
                    .append(e.getDateRecuperation() == null ? "null" : "\"" + escapeJson(e.getDateRecuperation()) + "\"")
                    .append(",")
                    .append("\"statut\":\"").append(escapeJson(e.getStatut())).append("\"")
                    .append("}");
            if (i < events.size() - 1) {
                sb.append(",");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    private String escapeJson(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
