package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import models.User;
import services.UserService;
import utils.InputValidator;
import utils.NavigationService;
import utils.UserSession;
import utils.AlertUtils;

public class SignUpController {

    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private ComboBox<String> roleCombo;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    
    // Captcha elements
    @FXML private javafx.scene.canvas.Canvas captchaCanvas;
    @FXML private TextField captchaField;
    @FXML private Label captchaError;
    private String currentCaptchaWord = "";

    @FXML private ImageView avatarImageView;
    @FXML private Label avatarPlaceholder;
    private String selectedImagePath = "";

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
        generateCaptcha();
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

    @FXML
    public void handleRefreshCaptcha(javafx.scene.input.MouseEvent event) {
        generateCaptcha();
    }
    
    @FXML
    public void handleRefreshCaptcha(ActionEvent event) {
        generateCaptcha();
    }

    private void generateCaptcha() {
        // Generate a random 6 character string (alphanumeric)
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; // Removed similar looking chars (I, 1, O, 0)
        StringBuilder sb = new StringBuilder();
        java.util.Random rnd = new java.util.Random();
        while (sb.length() < 6) { 
            int index = (int) (rnd.nextFloat() * chars.length());
            sb.append(chars.charAt(index));
        }
        currentCaptchaWord = sb.toString();
        drawCaptchaOnCanvas();
    }

    private void drawCaptchaOnCanvas() {
        if (captchaCanvas == null) return;
        javafx.scene.canvas.GraphicsContext gc = captchaCanvas.getGraphicsContext2D();
        double width = captchaCanvas.getWidth();
        double height = captchaCanvas.getHeight();

        // Background
        gc.setFill(javafx.scene.paint.Color.web("#f4f4f4"));
        gc.fillRect(0, 0, width, height);

        java.util.Random rnd = new java.util.Random();

        // Draw noise lines
        gc.setStroke(javafx.scene.paint.Color.web("#bdc3c7"));
        gc.setLineWidth(1.5);
        for (int i = 0; i < 6; i++) {
            gc.strokeLine(rnd.nextDouble() * width, rnd.nextDouble() * height,
                          rnd.nextDouble() * width, rnd.nextDouble() * height);
        }

        // Draw text
        gc.setFont(javafx.scene.text.Font.font("Courier New", javafx.scene.text.FontWeight.BOLD, 22));
        double xOffset = 15;
        for (char c : currentCaptchaWord.toCharArray()) {
            // Random color
            gc.setFill(javafx.scene.paint.Color.color(rnd.nextDouble() * 0.5, rnd.nextDouble() * 0.5, rnd.nextDouble() * 0.5));
            // Random rotation (simulate with y offset)
            double yOffset = 30 + (rnd.nextDouble() * 10 - 5);
            gc.fillText(String.valueOf(c), xOffset, yOffset);
            xOffset += 20;
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
    public void handleSelectAvatar(ActionEvent event) {
        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Choisir une photo de profil");
        fileChooser.getExtensionFilters().addAll(
            new javafx.stage.FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg")
        );
        
        java.io.File file = fileChooser.showOpenDialog(((javafx.scene.Node)event.getSource()).getScene().getWindow());
        
        if (file != null) {
            selectedImagePath = file.toURI().toString();
            avatarImageView.setImage(new javafx.scene.image.Image(selectedImagePath));
            avatarImageView.setVisible(true);
            avatarPlaceholder.setVisible(false);
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
        String captchaInput = captchaField.getText().trim();

        boolean hasError = false;

        // Captcha validation
        if (captchaInput.isEmpty() || !captchaInput.equalsIgnoreCase(currentCaptchaWord)) {
            showError(captchaError, "Code de sécurité incorrect.");
            hasError = true;
            generateCaptcha(); // Regenerate on failure
            captchaField.clear();
        } else {
            hideError(captchaError);
        }

        if (nom.isEmpty()) {
            showError(nomError, "Ce champ est requis");
            hasError = true;
        } else if (!InputValidator.isValidName(nom)) {
            showError(nomError, "Le nom doit contenir 2-50 lettres.");
            hasError = true;
        }
        
        if (prenom.isEmpty()) {
            showError(prenomError, "Ce champ est requis");
            hasError = true;
        } else if (!InputValidator.isValidName(prenom)) {
            showError(prenomError, "Le prénom doit contenir 2-50 lettres.");
            hasError = true;
        }
        
        if (email.isEmpty()) {
            showError(emailError, "Ce champ est requis");
            hasError = true;
        } else if (!InputValidator.isValidEmail(email)) {
            showError(emailError, "Format d'email invalide.");
            hasError = true;
        } else if (userService.existsByEmail(email)) {
            showError(emailError, "Cet email est déjà utilisé.");
            hasError = true;
        }

        if (phone.isEmpty()) {
            showError(phoneError, "Ce champ est requis");
            hasError = true;
        } else if (!InputValidator.isValidPhone(phone)) {
            showError(phoneError, "Téléphone invalide (Ex: +216 22123456).");
            hasError = true;
        }
        
        if (pass.isEmpty()) {
            showError(passwordError, "Ce champ est requis");
            hasError = true;
        } else if (!InputValidator.isValidPassword(pass)) {
            showError(passwordError, "Min 8 car, Majuscule, Chiffre, Symbole.");
            hasError = true;
        } 
        
        if (confirmPass.isEmpty()) {
            showError(confirmPasswordError, "Ce champ est requis");
            hasError = true;
        } else if (!pass.equals(confirmPass)) {
            showError(confirmPasswordError, "Les mots de passe ne correspondent pas.");
            hasError = true;
        }

        if (hasError) return;

        // Save logic
        String cleanedPhone = InputValidator.cleanPhone(phone);
        User newUser = new User(email, pass, role, nom, prenom, "Tunisie", "Nouveau membre", "Agriculteur", cleanedPhone);
        newUser.setImage(selectedImagePath);

        try {
            userService.add(newUser);
            UserSession.getInstance().setUser(newUser);
            
            AlertUtils.showSuccess("Inscription Réussie", "Bienvenue " + prenom + " ! Votre compte a été créé.");
            
            NavigationService.switchScene(event, "/esprit/tn/fxml/login.fxml", "Connexion");
        } catch (Exception e) {
            AlertUtils.showError("Erreur", "Une erreur est survenue lors de la sauvegarde.");
        }
    }

    @FXML
    public void goToLogin(ActionEvent event) {
        NavigationService.switchScene(event, "/esprit/tn/fxml/login.fxml", "Connexion");
    }

    @FXML
    public void goHome(ActionEvent event) {
        NavigationService.switchScene(event, "/esprit/tn/fxml/home.fxml", "Bienvenue");
    }
}

