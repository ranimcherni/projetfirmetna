package utils;

import controllers.CustomDialogController;
import javafx.animation.FadeTransition;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

public class AlertUtils {

    public static void showSuccess(String title, String message) {
        showCustomAlert(title, message, "success");
    }

    public static void showError(String title, String message) {
        showCustomAlert(title, message, "error");
    }

    public static boolean showConfirmation(String title, String message) {
        return showCustomAlert(title, message, "confirm");
    }

    private static boolean showCustomAlert(String title, String message, String type) {
        try {
            FXMLLoader loader = new FXMLLoader(AlertUtils.class.getResource("/esprit/tn/fxml/custom_dialog.fxml"));
            Parent root = loader.load();

            CustomDialogController controller = loader.getController();
            controller.setContent(title, message, type);

            Stage stage = new Stage();
            stage.initStyle(StageStyle.TRANSPARENT);
            stage.initModality(Modality.APPLICATION_MODAL);
            
            Scene scene = new Scene(root);
            scene.getStylesheets().add(AlertUtils.class.getResource("/esprit/tn/css/style.css").toExternalForm());
            scene.setFill(Color.TRANSPARENT);
            stage.setScene(scene);

            // Entry Animation
            root.setOpacity(0);
            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), root);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            fadeIn.play();

            stage.showAndWait();
            return controller.isConfirmed();

        } catch (Exception e) {
            e.printStackTrace();
            // Fallback to basic if custom fails
            return false;
        }
    }
}
