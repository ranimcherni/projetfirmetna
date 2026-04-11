package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import models.User;
import services.UserService;
import utils.InputValidator;
import utils.NavigationService;
import utils.UserSession;

public class SignUpController {

    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private ComboBox<String> roleCombo;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;

    // Error Labels
    @FXML private Label nomError;
    @FXML private Label prenomError;
    @FXML private Label emailError;
    @FXML private Label phoneError;
    @FXML private Label passwordError;
    @FXML private Label confirmPasswordError;

    private UserService userService = new UserService();

    @FXML
    public void initialize() {
        resetErrorLabels();
    }

    private void resetErrorLabels() {
        hideError(nomError);
        hideError(prenomError);
        hideError(emailError);
        hideError(phoneError);
        hideError(passwordError);
        hideError(confirmPasswordError);
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
    public void handleSignUp(ActionEvent event) {
        resetErrorLabels();

        String nom = nomField.getText().trim();
        String prenom = prenomField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        String role = roleCombo.getValue();
        String pass = passwordField.getText();
        String confirmPass = confirmPasswordField.getText();

        boolean hasError = false;

        if (!InputValidator.isValidName(nom)) {
            showError(nomError, "Le nom doit contenir 2-50 lettres.");
            hasError = true;
        }
        
        if (!InputValidator.isValidName(prenom)) {
            showError(prenomError, "Le prénom doit contenir 2-50 lettres.");
            hasError = true;
        }
        
        if (!InputValidator.isValidEmail(email)) {
            showError(emailError, "Format d'email invalide.");
            hasError = true;
        } else if (userService.existsByEmail(email)) {
            showError(emailError, "Cet email est déjà utilisé.");
            hasError = true;
        }

        if (!InputValidator.isValidPhone(phone)) {
            showError(phoneError, "Téléphone invalide (Ex: +216 22123456).");
            hasError = true;
        }
        
        if (!InputValidator.isValidPassword(pass)) {
            showError(passwordError, "Mot de passe requis (Min 8 car, Maj, Chiffre, Symbole).");
            hasError = true;
        } else if (!pass.equals(confirmPass)) {
            showError(confirmPasswordError, "Les mots de passe ne correspondent pas.");
            hasError = true;
        }

        if (hasError) return;

        // Save logic
        String cleanedPhone = InputValidator.cleanPhone(phone);
        User newUser = new User(email, pass, role, nom, prenom, "Tunisie", "Nouveau membre", "Agriculteur", cleanedPhone);

        try {
            userService.add(newUser);
            UserSession.getInstance().setUser(newUser);
            
            Alert success = new Alert(Alert.AlertType.INFORMATION);
            success.setTitle("Inscription Réussie");
            success.setHeaderText(null);
            success.setContentText("Bienvenue " + prenom + " ! Votre compte a été créé.");
            success.showAndWait();
            
            NavigationService.switchScene(event, "/esprit/tn/fxml/login.fxml", "Connexion");
        } catch (Exception e) {
            Alert error = new Alert(Alert.AlertType.ERROR);
            error.setTitle("Erreur");
            error.setContentText("Une erreur est survenue lors de la sauvegarde.");
            error.show();
        }
    }

    @FXML
    public void goToLogin(ActionEvent event) {
        NavigationService.switchScene(event, "/esprit/tn/fxml/login.fxml", "Connexion");
    }
}
