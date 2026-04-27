package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

public class CustomDialogController {

    @FXML private StackPane rootPane;
    @FXML private Circle iconBg;
    @FXML private Label iconLabel;
    @FXML private Label titleLabel;
    @FXML private Label messageLabel;
    @FXML private Button actionBtn;
    @FXML private Button cancelBtn;

    private boolean confirmed = false;

    public void setContent(String title, String message, String type) {
        titleLabel.setText(title);
        messageLabel.setText(message);

        switch (type.toLowerCase()) {
            case "success":
                iconLabel.setText("✅");
                iconBg.setFill(Color.web("#27ae60"));
                break;
            case "error":
                iconLabel.setText("❌");
                iconBg.setFill(Color.web("#e74c3c"));
                break;
            case "confirm":
                iconLabel.setText("❓");
                iconBg.setFill(Color.web("#3498db"));
                cancelBtn.setVisible(true);
                cancelBtn.setManaged(true);
                break;
            default:
                iconLabel.setText("ℹ️");
                iconBg.setFill(Color.web("#27ae60"));
                break;
        }
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    @FXML
    private void handleAction() {
        confirmed = true;
        close();
    }

    @FXML
    private void handleCancel() {
        confirmed = false;
        close();
    }

    private void close() {
        Stage stage = (Stage) rootPane.getScene().getWindow();
        stage.close();
    }
}
