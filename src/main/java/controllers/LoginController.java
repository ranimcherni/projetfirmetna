package controllers;

import java.util.List;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import models.User;
import services.UserService;
import utils.UserSession;
import utils.NavigationService;
import utils.InputValidator;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import utils.AlertUtils;

public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;
    
    // Inline Error Labels
    @FXML private Label emailError;
    @FXML private Label passwordError;
    
    @FXML private Label faceLoginStatus;

    @FXML
    public void initialize() {
        resetErrorLabels();
    }

    private void resetErrorLabels() {
        hideError(emailError);
        hideError(passwordError);
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }

    private void hideError(Label label) {
        if (label != null) {
            label.setVisible(false);
            label.setManaged(false);
        }
    }

    private void showError(Label label, String message) {
        if (label != null) {
            label.setText(message);
            label.setVisible(true);
            label.setManaged(true);
        }
    }

    @FXML
    public void handleFaceLogin(ActionEvent event) {
        resetErrorLabels();
        String email = emailField.getText().trim();
        
        if (email.isEmpty()) {
            showGeneralError("Veuillez entrer votre email avant la connexion par visage.");
            return;
        } else if (!InputValidator.isValidEmail(email)) {
            showGeneralError("Veuillez saisir un email valide.");
            return;
        }

        UserService us = new UserService();
        User targetUser = us.getAll().stream().filter(u -> u.getEmail().equalsIgnoreCase(email)).findFirst().orElse(null);

        if (targetUser == null) {
            showGeneralError("Cet email n'existe pas dans notre système.");
            return;
        }

        if ("Inactif".equalsIgnoreCase(targetUser.getStatus())) {
            showGeneralError("Désolé, votre compte est bloqué.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/esprit/tn/fxml/face_capture_dialog.fxml"));
            Parent root = loader.load();
            
            FaceCaptureDialogController dialogController = loader.getController();
            dialogController.initData(false); // false = Login Mode
            
            Stage dialogStage = new Stage();
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.initStyle(javafx.stage.StageStyle.TRANSPARENT);
            
            Scene scene = new Scene(root);
            scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
            dialogStage.setScene(scene);
            
            dialogStage.showAndWait();
            
            if (dialogController.isSuccessful()) {
                int recognizedUserId = dialogController.getRecognizedUserId();
                if (recognizedUserId > 0) {
                    // VÉRIFICATION STRICTE 1-à-1
                    if (recognizedUserId == targetUser.getId()) {
                        UserSession.getInstance().setUser(targetUser);
                        goToFront(event); // Redirect to front/dashboard
                    } else {
                        // Le visage reconnu ne correspond pas à l'email entré
                        showGeneralError("Accès refusé : Ce visage ne correspond pas à l'email fourni.");
                    }
                } else {
                    showGeneralError("Visage non reconnu.");
                }
            } else {
                if (faceLoginStatus != null) {
                    faceLoginStatus.setText("Échec ou annulation.");
                    faceLoginStatus.setVisible(true);
                    faceLoginStatus.setManaged(true);
                }
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showError("Erreur Système", "Impossible de démarrer la caméra.");
        }
    }

    @FXML
    public void handleLogin(ActionEvent event) {
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        
        resetErrorLabels();

        boolean hasError = false;

        // Validation Format
        if (email.isEmpty()) {
            showError(emailError, "Ce champ est requis");
            hasError = true;
        } else if (!InputValidator.isValidEmail(email)) {
            showError(emailError, "Veuillez saisir un email valide.");
            hasError = true;
        }

        if (password.isEmpty()) {
            showError(passwordError, "Ce champ est requis");
            hasError = true;
        }

        if (hasError) return;

        // Admin hardcoded login
        if (email.equals("admin@firmetna.com") && password.equals("admin123")) {
            User admin = new User();
            admin.setEmail(email);
            admin.setNom("Admin");
            admin.setPrenom("Système");
            admin.setRole("ADMIN");
            admin.setStatus("Actif");
            UserSession.getInstance().setUser(admin);
            goToAdmin(event);
            return;
        }

        // DB User login
        UserService us = new UserService();
        User targetUser = null;

        // 1. Chercher l'utilisateur par son email d'abord
        List<User> allUsers = us.getAll();
        for (User u : allUsers) {
            if (u.getEmail().equalsIgnoreCase(email)) {
                targetUser = u;
                break;
            }
        }

        if (targetUser != null) {
            // 2. Vérifier s'il est bloqué (Inactif)
            if ("Inactif".equalsIgnoreCase(targetUser.getStatus())) {
                showGeneralError("Désolé, votre compte est bloqué.");
                return;
            }

            // 3. Vérifier le mot de passe
            boolean isPasswordCorrect = false;
            try {
                if (targetUser.getPassword().startsWith("$2a$")) {
                    isPasswordCorrect = org.mindrot.jbcrypt.BCrypt.checkpw(password, targetUser.getPassword());
                } else {
                    isPasswordCorrect = targetUser.getPassword().equals(password);
                }
            } catch (Exception e) {
                isPasswordCorrect = targetUser.getPassword().equals(password);
            }

            if (isPasswordCorrect) {
                if (targetUser.isMfaEnabled()) {
                    try {
                        javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/esprit/tn/fxml/mfa_dialog.fxml"));
                        javafx.scene.Parent root = loader.load();
                        
                        MfaDialogController dialogController = loader.getController();
                        dialogController.initData(targetUser.getEmail(), targetUser.getMfaSecret(), false); // Verification mode
                        
                        javafx.stage.Stage stage = new javafx.stage.Stage();
                        stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
                        stage.initStyle(javafx.stage.StageStyle.TRANSPARENT);
                        
                        javafx.scene.Scene scene = new javafx.scene.Scene(root);
                        scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
                        stage.setScene(scene);
                        stage.showAndWait();

                        if (!dialogController.isSuccessful()) {
                            return; // Don't login if MFA failed or cancelled
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        showGeneralError("Erreur lors de la vérification MFA.");
                        return;
                    }
                }
                UserSession.getInstance().setUser(targetUser);
                goToFront(event);
            } else {
                showGeneralError("Mot de passe incorrect.");
            }
        } else {
            showGeneralError("Cet email n'existe pas dans notre système.");
        }
    }

    private void showGeneralError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void goToAdmin(ActionEvent event) {
        NavigationService.switchScene(event, "/esprit/tn/fxml/admin_layout.fxml", "Tableau de Bord");
    }

    @FXML
    public void goToSignUp(ActionEvent event) {
        NavigationService.switchScene(event, "/esprit/tn/fxml/signup.fxml", "Inscription");
    }

    @FXML
    public void goHome(ActionEvent event) {
        NavigationService.switchScene(event, "/esprit/tn/fxml/home.fxml", "Bienvenue");
    }

    @FXML
    public void goToFront(ActionEvent event) {
        NavigationService.switchScene(event, "/esprit/tn/fxml/front.fxml", "Accueil");
    }

    @FXML
    public void handleForgotPassword(ActionEvent event) {
        NavigationService.switchScene(event, "/esprit/tn/fxml/forgot_password.fxml", "Récupération de mot de passe");
    }
}

