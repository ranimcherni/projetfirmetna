package controllers;

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
        if (!InputValidator.isValidEmail(email)) {
            showError(emailError, "Veuillez saisir un email valide.");
            hasError = true;
        }

        if (password.isEmpty()) {
            showError(passwordError, "Mot de passe requis.");
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
        User foundUser = null;
        for (User u : us.getAll()) {
            if (u.getEmail().equals(email) && u.getPassword().equals(password)) {
                foundUser = u;
                break;
            }
        }

        if (foundUser != null) {
            String status = foundUser.getStatus();
            if (status != null && status.equalsIgnoreCase("Inactif")) {
                showGeneralError("Désolé, votre compte est bloqué.");
            } else {
                UserSession.getInstance().setUser(foundUser);
                goToFront(event);
            }
        } else {
            showGeneralError("Identifiants incorrects.");
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
