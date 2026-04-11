package utils;

import javafx.event.Event;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.MenuItem;
import javafx.stage.Stage;
import java.io.IOException;

public class NavigationService {

    public static final double DEFAULT_WIDTH = 1100;
    public static final double DEFAULT_HEIGHT = 730;

    public static void switchScene(Event event, String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(NavigationService.class.getResource(fxmlPath));
            Parent root = loader.load();

            Stage stage = getStageFromEvent(event);
            if (stage == null)
                return;

            Scene scene = new Scene(root, DEFAULT_WIDTH, DEFAULT_HEIGHT);
            stage.setTitle("Firmetna" + (title != null && !title.isEmpty() ? " - " + title : ""));
            stage.setScene(scene);

            // Full screen / Maximized
            stage.setMaximized(true);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Stage getStageFromEvent(Event event) {
        if (event == null)
            return null;
        Object source = event.getSource();

        if (source instanceof Node) {
            return (Stage) ((Node) source).getScene().getWindow();
        } else if (source instanceof MenuItem) {
            // MenuItem is not a Node, we need to get the stage via the parent menu's owner
            return (Stage) ((MenuItem) source).getParentPopup().getOwnerWindow();
        }
        return null;
    }

    public static void navigateFromNode(Node anchor, String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(NavigationService.class.getResource(fxmlPath));
            Parent root = loader.load();

            Stage stage = (Stage) anchor.getScene().getWindow();
            Scene scene = new Scene(root, DEFAULT_WIDTH, DEFAULT_HEIGHT);

            stage.setTitle("Firmetna" + (title != null && !title.isEmpty() ? " - " + title : ""));
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
