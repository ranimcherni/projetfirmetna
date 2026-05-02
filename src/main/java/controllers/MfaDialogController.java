package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import services.GoogleAuthService;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.KeyCode;

public class MfaDialogController {

    @FXML private VBox rootVBox;
    @FXML private VBox setupBox;
    @FXML private Label secretKeyLabel;
    @FXML private ImageView qrCodeView;
    @FXML private Label errorLabel;
    
    @FXML private TextField code1;
    @FXML private TextField code2;
    @FXML private TextField code3;
    @FXML private TextField code4;
    @FXML private TextField code5;
    @FXML private TextField code6;

    private GoogleAuthService googleAuthService = new GoogleAuthService();
    private String currentSecret;
    private boolean successful = false;
    private boolean isSetupMode = false;
    private TextField[] inputFields;

    private double xOffset = 0;
    private double yOffset = 0;

    @FXML
    public void initialize() {
        inputFields = new TextField[]{code1, code2, code3, code4, code5, code6};
        
        // Window dragging
        rootVBox.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });
        rootVBox.setOnMouseDragged(event -> {
            Stage stage = (Stage) rootVBox.getScene().getWindow();
            stage.setX(event.getScreenX() - xOffset);
            stage.setY(event.getScreenY() - yOffset);
        });

        for (int i = 0; i < inputFields.length; i++) {
            final int index = i;
            TextField field = inputFields[i];
            
            // Limit to 1 character
            field.textProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal.length() > 1) {
                    field.setText(newVal.substring(0, 1));
                }
                if (newVal.length() == 1 && index < 5) {
                    inputFields[index + 1].requestFocus();
                }
                
                // Auto-verify if last digit is entered
                if (getFullCode().length() == 6) {
                    handleVerify();
                }
            });

            // Backspace handling
            field.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
                if (event.getCode() == KeyCode.BACK_SPACE && field.getText().isEmpty() && index > 0) {
                    inputFields[index - 1].requestFocus();
                }
            });
        }
    }

    public void initData(String email, String secret, boolean setupMode) {
        this.currentSecret = secret;
        this.isSetupMode = setupMode;

        if (isSetupMode) {
            setupBox.setVisible(true);
            setupBox.setManaged(true);
            secretKeyLabel.setText(currentSecret);
            
            String otpauthUrl = googleAuthService.getOtpAuthURL(email, secret);
            try {
                String qrUrl = "https://api.qrserver.com/v1/create-qr-code/?size=160x160&data=" + java.net.URLEncoder.encode(otpauthUrl, "UTF-8");
                qrCodeView.setImage(new javafx.scene.image.Image(qrUrl));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        // Focus first field
        javafx.application.Platform.runLater(() -> code1.requestFocus());
    }

    private String getFullCode() {
        StringBuilder sb = new StringBuilder();
        for (TextField f : inputFields) {
            sb.append(f.getText());
        }
        return sb.toString();
    }

    @FXML
    private void handleVerify() {
        String codeStr = getFullCode();
        if (codeStr.length() != 6 || !codeStr.matches("\\d+")) {
            errorLabel.setText("Veuillez entrer 6 chiffres.");
            errorLabel.setVisible(true);
            return;
        }

        try {
            int code = Integer.parseInt(codeStr);
            if (googleAuthService.authorize(currentSecret, code)) {
                successful = true;
                closeDialog();
            } else {
                errorLabel.setText("Code incorrect ou expiré.");
                errorLabel.setVisible(true);
                // Clear fields on error for better UX
                for (TextField f : inputFields) f.clear();
                code1.requestFocus();
            }
        } catch (NumberFormatException e) {
            errorLabel.setText("Format invalide.");
            errorLabel.setVisible(true);
        }
    }

    @FXML
    private void handleCancel() {
        successful = false;
        closeDialog();
    }

    private void closeDialog() {
        ((Stage) rootVBox.getScene().getWindow()).close();
    }

    public boolean isSuccessful() {
        return successful;
    }
}
