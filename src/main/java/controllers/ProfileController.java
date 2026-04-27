package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import models.User;
import services.UserService;
import utils.UserSession;
import utils.NavigationService;
import utils.InputValidator;
import utils.AlertUtils;

public class ProfileController {

    @FXML private Label headerNameLabel;
    @FXML private Label headerRoleLabel;
    @FXML private TextField prenomField;
    @FXML private TextField nomField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;

    @FXML private Label mfaStatusLabel;
    @FXML private Button mfaButton;

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
            updateMfaUI();
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

        if (nom.isEmpty()) {
            showError(nomError, "Ce champ est requis");
            hasError = true;
        } else if (!InputValidator.isValidName(nom)) {
            showError(nomError, "Nom invalide (2-50 lettres).");
            hasError = true;
        }
        
        if (prenom.isEmpty()) {
            showError(prenomError, "Ce champ est requis");
            hasError = true;
        } else if (!InputValidator.isValidName(prenom)) {
            showError(prenomError, "Prénom invalide (2-50 lettres).");
            hasError = true;
        }
        
        if (email.isEmpty()) {
            showError(emailError, "Ce champ est requis");
            hasError = true;
        } else if (!InputValidator.isValidEmail(email)) {
            showError(emailError, "Format e-mail invalide.");
            hasError = true;
        } else if (userService.isEmailTaken(email, currentUser.getId())) {
            showError(emailError, "Cet email est déjà utilisé par un autre compte.");
            hasError = true;
        }

        if (phone.isEmpty()) {
            showError(phoneError, "Ce champ est requis");
            hasError = true;
        } else if (!InputValidator.isValidPhone(phone)) {
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
        AlertUtils.showSuccess("Succès", "Votre profil a été mis à jour avec succès !");
        loadUserData(); // Refresh labels
    }

    @FXML
    private void handleDelete(ActionEvent event) {
        if (AlertUtils.showConfirmation("Confirmation de suppression", 
                "Supprimer votre compte ?\nCette action est irréversible. Toutes vos données seront perdues.")) {
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

    private void updateMfaUI() {
        if (currentUser.isMfaEnabled()) {
            mfaStatusLabel.setText("Activé - Votre compte est sécurisé");
            mfaStatusLabel.setStyle("-fx-text-fill: #27ae60;");
            mfaButton.setText("Désactiver");
            mfaButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-background-radius: 20;");
        } else {
            mfaStatusLabel.setText("Désactivé - Renforcez votre sécurité");
            mfaStatusLabel.setStyle("-fx-text-fill: #5a6268;");
            mfaButton.setText("Activer");
            mfaButton.setStyle("-fx-background-color: #0056b3; -fx-text-fill: white; -fx-background-radius: 20;");
        }
    }

    @FXML
    private void handleMfaToggle(ActionEvent event) {
        if (currentUser.isMfaEnabled()) {
            if (AlertUtils.showConfirmation("Désactiver le MFA", "Voulez-vous vraiment désactiver la double authentification ?")) {
                currentUser.setMfaEnabled(false);
                currentUser.setMfaSecret(null);
                userService.update(currentUser);
                updateMfaUI();
                AlertUtils.showSuccess("Sécurité", "Le MFA a été désactivé.");
            }
        } else {
            try {
                services.GoogleAuthService googleAuthService = new services.GoogleAuthService();
                String secret = googleAuthService.generateSecretKey();

                javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/esprit/tn/fxml/mfa_dialog.fxml"));
                javafx.scene.Parent root = loader.load();
                
                MfaDialogController dialogController = loader.getController();
                dialogController.initData(currentUser.getEmail(), secret, true); // Setup mode
                
                javafx.stage.Stage stage = new javafx.stage.Stage();
                stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
                stage.initStyle(javafx.stage.StageStyle.TRANSPARENT);
                
                javafx.scene.Scene scene = new javafx.scene.Scene(root);
                scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
                stage.setScene(scene);
                stage.showAndWait();

                if (dialogController.isSuccessful()) {
                    currentUser.setMfaEnabled(true);
                    currentUser.setMfaSecret(secret);
                    userService.update(currentUser);
                    updateMfaUI();
                    AlertUtils.showSuccess("Sécurité", "Le MFA a été activé avec succès !");
                }
            } catch (Exception e) {
                e.printStackTrace();
                AlertUtils.showError("Erreur", "Impossible d'ouvrir la configuration MFA.");
            }
        }
    }

}
