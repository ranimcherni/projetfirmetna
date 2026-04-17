package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import services.UserService;
import utils.InputValidator;
import utils.MailingService;
import utils.NavigationService;

import java.sql.Timestamp;
import java.util.Random;

public class ForgotPasswordController {

    @FXML private VBox step1Box;
    @FXML private VBox step2Box;
    @FXML private VBox step3Box;

    @FXML private TextField emailField;
    @FXML private TextField codeField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;

    @FXML private Label instructionLabel;
    @FXML private Label errorLabel;
    @FXML private Label successLabel;

    private UserService userService;
    private String verifiedEmail;

    @FXML
    public void initialize() {
        userService = new UserService();
        showStep1();
    }

    private void hideMessages() {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
        successLabel.setVisible(false);
        successLabel.setManaged(false);
    }

    private void showError(String message) {
        hideMessages();
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void showSuccess(String message) {
        hideMessages();
        successLabel.setText(message);
        successLabel.setVisible(true);
        successLabel.setManaged(true);
    }

    private void showStep1() {
        step1Box.setVisible(true);
        step1Box.setManaged(true);
        step2Box.setVisible(false);
        step2Box.setManaged(false);
        step3Box.setVisible(false);
        step3Box.setManaged(false);
        instructionLabel.setText("Entrez votre email pour recevoir un code de vérification.");
    }

    private void showStep2() {
        step1Box.setVisible(false);
        step1Box.setManaged(false);
        step2Box.setVisible(true);
        step2Box.setManaged(true);
        step3Box.setVisible(false);
        step3Box.setManaged(false);
        instructionLabel.setText("Entrez le code à 6 chiffres envoyé à votre adresse email.");
    }

    private void showStep3() {
        step1Box.setVisible(false);
        step1Box.setManaged(false);
        step2Box.setVisible(false);
        step2Box.setManaged(false);
        step3Box.setVisible(true);
        step3Box.setManaged(true);
        instructionLabel.setText("Créez votre nouveau mot de passe.");
    }

    @FXML
    public void handleSendCode(ActionEvent event) {
        String email = emailField.getText().trim();

        if (email.isEmpty() || !InputValidator.isValidEmail(email)) {
            showError("Veuillez entrer une adresse email valide.");
            return;
        }

        if (!userService.existsByEmail(email)) {
            showError("Aucun compte n'est associé à cette adresse email.");
            return;
        }

        // Generate 6-digit code
        String code = String.format("%06d", new Random().nextInt(999999));
        
        // Expiry = current time + 10 minutes (600,000 ms)
        Timestamp expiry = new Timestamp(System.currentTimeMillis() + 600000);

        try {
            userService.setResetCode(email, code, expiry);
            
            // SIMULATION POUR LE TEST : On affiche le code dans la console au lieu de l'envoyer
            System.out.println("\n--------------------------------------------------");
            System.out.println("🔧 MODE TEST - SIMULATION D'ENVOI D'EMAIL");
            System.out.println("📧 Destinataire : " + email);
            System.out.println("🔑 CODE DE VÉRIFICATION : " + code);
            System.out.println("--------------------------------------------------\n");
            
            // MailingService.sendVerificationCode(email, code); // Désactivé temporairement
            
            verifiedEmail = email;
            showSuccess("Code généré ! (Regardez la console de VS Code pour le voir)");
            showStep2();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur lors de la génération du code.");
        }
    }

    @FXML
    public void handleVerifyCode(ActionEvent event) {
        String code = codeField.getText().trim();

        if (code.length() != 6) {
            showError("Le code doit contenir 6 chiffres.");
            return;
        }

        if (userService.validateResetCode(verifiedEmail, code)) {
            showSuccess("Code valide ! Vous pouvez changer votre mot de passe.");
            showStep3();
        } else {
            showError("Code invalide ou expiré.");
        }
    }

    @FXML
    public void handleResetPassword(ActionEvent event) {
        String password = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (password.length() < 6) {
            showError("Le mot de passe doit contenir au moins 6 caractères.");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showError("Les mots de passe ne correspondent pas.");
            return;
        }

        userService.updatePasswordByEmail(verifiedEmail, password);
        showSuccess("Mot de passe modifié avec succès !");
        
        // Return to login after 2 seconds or so (for simplicity, we just return immediately)
        goBackToLogin(event);
    }

    @FXML
    public void goBackToLogin(ActionEvent event) {
        NavigationService.switchScene(event, "/esprit/tn/fxml/Login.fxml", "Connexion");
    }
}
