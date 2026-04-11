package controllers;

import java.util.List;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import models.User;
import services.UserService;
import utils.UserSession;
import utils.NavigationService;
import utils.InputValidator;

public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;
    
    // Inline Error Labels
    @FXML private Label emailError;
    @FXML private Label passwordError;

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
}
