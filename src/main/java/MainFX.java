// No package to match simple architecture

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainFX extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            java.net.URL resource = getClass().getResource("/esprit/tn/fxml/home.fxml");
            if (resource == null) {
                showError(primaryStage, "Impossible de trouver home.fxml dans le dossier resources/esprit/tn/fxml/");
                return;
            }
            FXMLLoader loader = new FXMLLoader(resource);
            Parent root = loader.load();
            Scene scene = new Scene(root, 1100, 700);
            scene.setFill(javafx.scene.paint.Color.WHITE);
            primaryStage.setTitle("Firmetna - Bienvenue");
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showError(primaryStage, "Erreur de chargement de l'interface : " + e.getMessage());
        }
    }

    private void showError(Stage stage, String message) {
        javafx.scene.layout.VBox root = new javafx.scene.layout.VBox(20);
        root.setAlignment(javafx.geometry.Pos.CENTER);
        root.setStyle("-fx-background-color: #e74c3c; -fx-padding: 50;");
        
        javafx.scene.control.Label title = new javafx.scene.control.Label("OUPS ! ERREUR CRITIQUE");
        title.setStyle("-fx-text-fill: white; -fx-font-size: 24; -fx-font-weight: bold;");
        
        javafx.scene.control.Label msg = new javafx.scene.control.Label(message);
        msg.setStyle("-fx-text-fill: white; -fx-font-size: 16;");
        msg.setWrapText(true);
        
        root.getChildren().addAll(title, msg);
        stage.setScene(new Scene(root, 800, 400));
        stage.setTitle("Erreur de lancement");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
