package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import models.User;
import services.UserService;
import utils.UserSession;
import utils.NavigationService;
import utils.InputValidator;

public class ProfileController {

    @FXML private Label headerNameLabel;
    @FXML private Label headerRoleLabel;
    @FXML private TextField prenomField;
    @FXML private TextField nomField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;

    // Error Labels
    @FXML private Label nomError;
    @FXML private Label prenomError;
    @FXML private Label emailError;
    @FXML private Label phoneError;

    private UserService userService = new UserService();
    private User currentUser;

    @FXML
    public void initialize() {
        currentUser = UserSession.getInstance().getUser();
        if (currentUser != null) {
            loadUserData();
        }
        resetErrorLabels();
    }

    private void resetErrorLabels() {
        hideError(nomError);
        hideError(prenomError);
        hideError(emailError);
        hideError(phoneError);
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

    private void loadUserData() {
        headerNameLabel.setText(currentUser.getPrenom() + " " + currentUser.getNom());
        headerRoleLabel.setText(currentUser.getRole());
        
        prenomField.setText(currentUser.getPrenom());
        nomField.setText(currentUser.getNom());
        emailField.setText(currentUser.getEmail());
        phoneField.setText(currentUser.getTelephone() != null ? currentUser.getTelephone() : "");
    }

    @FXML
    private void handleUpdate(ActionEvent event) {
        resetErrorLabels();

        String nom = nomField.getText().trim();
        String prenom = prenomField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();

        boolean hasError = false;

        if (!InputValidator.isValidName(nom)) {
            showError(nomError, "Nom invalide (2-50 lettres).");
            hasError = true;
        }
        
        if (!InputValidator.isValidName(prenom)) {
            showError(prenomError, "Prénom invalide (2-50 lettres).");
            hasError = true;
        }
        
        if (!InputValidator.isValidEmail(email)) {
            showError(emailError, "Format e-mail invalide.");
            hasError = true;
        } else if (userService.isEmailTaken(email, currentUser.getId())) {
            showError(emailError, "Cet email est déjà utilisé par un autre compte.");
            hasError = true;
        }

        if (!InputValidator.isValidPhone(phone)) {
            showError(phoneError, "Téléphone Tunisien invalide.");
            hasError = true;
        }

        if (hasError) return;

        // Perform Update
        currentUser.setPrenom(prenom);
        currentUser.setNom(nom);
        currentUser.setEmail(email);
        currentUser.setTelephone(InputValidator.cleanPhone(phone));

        userService.update(currentUser);
        showAlert(Alert.AlertType.INFORMATION, "Succès", "Votre profil a été mis à jour avec succès !");
        loadUserData(); // Refresh labels
    }

    @FXML
    private void handleDelete(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Supprimer votre compte ?");
        alert.setContentText("Cette action est irréversible. Toutes vos données seront perdues.");

        if (alert.showAndWait().get() == ButtonType.OK) {
            userService.delete(currentUser);
            UserSession.getInstance().cleanUserSession();
            handleLogout(event);
        }
    }

    @FXML
    private void handleAccueil(ActionEvent event) {
        NavigationService.switchScene(event, "/esprit/tn/fxml/front.fxml", "Accueil");
    }

    @FXML
    private void handleBack(ActionEvent event) {
        handleAccueil(event);
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        UserSession.getInstance().cleanUserSession();
        NavigationService.switchScene(event, "/esprit/tn/fxml/home.fxml", "Bienvenue");
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
