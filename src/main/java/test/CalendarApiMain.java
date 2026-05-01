package test;

import controllers.CalendarApiController;

public class CalendarApiMain {
    public static void main(String[] args) {
        try {
            new CalendarApiController().startServer(8081);
        } catch (Exception e) {
            System.err.println("Impossible de demarrer Calendar API: " + e.getMessage());
        }
    }
}
