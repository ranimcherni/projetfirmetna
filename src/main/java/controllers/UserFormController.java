package controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import models.User;
import services.UserService;
import utils.InputValidator;

public class UserFormController {

    @FXML private Label titleLabel;
    @FXML private Label subtitleLabel;
    @FXML private TextField prenomField;
    @FXML private TextField nomField;
    @FXML private TextField emailField;
    @FXML private TextField telephoneField;
    @FXML private PasswordField passwordField;
    @FXML private ComboBox<String> typeCombo;
    @FXML private TableColumn<User, String> statusCol;
    @FXML private TableColumn<User, String> telCol;
    @FXML private TableColumn<User, java.sql.Timestamp> dateCol;
    @FXML private ComboBox<String> statusCombo;
    @FXML private ComboBox<String> systemRoleCombo;
    @FXML private VBox typeContainer;
    @FXML private VBox passwordInfoBox;
    @FXML private Button saveBtn;

    // Error Labels
    @FXML private Label nomError;
    @FXML private Label prenomError;
    @FXML private Label emailError;
    @FXML private Label telephoneError;
    @FXML private Label passwordError;

    private UserService userService = new UserService();
    private User currentUser;
    private boolean isEdit = false;

    @FXML
    public void initialize() {
        typeCombo.setItems(FXCollections.observableArrayList("Agriculteur", "Client", "Donateur"));
        statusCombo.setItems(FXCollections.observableArrayList("Actif", "Inactif"));
        systemRoleCombo.setItems(FXCollections.observableArrayList("Utilisateur", "Administrateur", "Modérateur"));
        
        typeCombo.getSelectionModel().select(0);
        statusCombo.getSelectionModel().select(0);
        systemRoleCombo.getSelectionModel().select(0);

        resetErrorLabels();
        
        // Ensure +216 is always present
        if (telephoneField.getText().isEmpty()) {
            telephoneField.setText("+216");
        }
        
        telephoneField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.startsWith("+216")) {
                telephoneField.setText("+216");
            }
        });
    }

    private void resetErrorLabels() {
        hideError(nomError);
        hideError(prenomError);
        hideError(emailError);
        hideError(telephoneError);
        hideError(passwordError);
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

    public void setUser(User user) {
        this.currentUser = user;
        this.isEdit = true;
        this.titleLabel.setText("Modifier l'utilisateur #" + user.getId());
        this.subtitleLabel.setText("Mettez à jour les informations du compte.");
        this.saveBtn.setText("✔ Mettre à jour");
        
        prenomField.setText(user.getPrenom());
        nomField.setText(user.getNom());
        emailField.setText(user.getEmail());
        telephoneField.setText(user.getTelephone());
        
        typeCombo.setValue(user.getRole());
        statusCombo.setValue(user.getStatus() != null ? user.getStatus() : "Actif");
        
        typeContainer.setDisable(true);
        typeContainer.setOpacity(0.6);
        
        passwordInfoBox.setVisible(true);
        passwordInfoBox.setManaged(true);
        passwordField.setPromptText("Laissez vide pour conserver");
    }

    @FXML
    private void handleSave() {
        if (isValid()) {
            if (!isEdit) {
                currentUser = new User();
            }
            
            currentUser.setPrenom(prenomField.getText().trim());
            currentUser.setNom(nomField.getText().trim());
            currentUser.setEmail(emailField.getText().trim());
            currentUser.setTelephone(telephoneField.getText().trim());
            currentUser.setRole(typeCombo.getValue());
            currentUser.setStatus(statusCombo.getValue());

            String password = passwordField.getText();
            if (!isEdit) {
                currentUser.setPassword(password);
                userService.add(currentUser);
            } else {
                if (!password.isEmpty()) {
                    currentUser.setPassword(password);
                }
                userService.update(currentUser);
            }

            goBack();
        }
    }

    @FXML
    private void handleCancel() {
        goBack();
    }

    private void goBack() {
        AdminLayoutController.getInstance().loadView("/esprit/tn/fxml/admin_users.fxml");
    }

    private boolean isValid() {
        resetErrorLabels();
        
        String nom = nomField.getText().trim();
        String prenom = prenomField.getText().trim();
        String email = emailField.getText().trim();
        String tel = telephoneField.getText().trim();
        String pass = passwordField.getText();

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
        } else {
            boolean taken = isEdit ? userService.isEmailTaken(email, currentUser.getId()) : userService.existsByEmail(email);
            if (taken) {
                showError(emailError, "Cet email est déjà utilisé.");
                hasError = true;
            }
        }

        // Telephone validation
        if (tel.equals("+216")) {
            showError(telephoneError, "Ce champ est requis");
            hasError = true;
        } else if (!InputValidator.isValidPhone(tel)) {
            showError(telephoneError, "Doit contenir exactement 8 chiffres après +216");
            hasError = true;
        }

        // Passwords
        if (!isEdit && pass.isEmpty()) {
            showError(passwordError, "Ce champ est requis");
            hasError = true;
        } else if (isEdit && !pass.isEmpty()) {
            if (!InputValidator.isValidPassword(pass)) {
                showError(passwordError, "Mot de passe trop faible.");
                hasError = true;
            }
        } else if (!isEdit) {
            if (!InputValidator.isValidPassword(pass)) {
                showError(passwordError, "Mot de passe fort obligatoire.");
                hasError = true;
            }
        }

        return !hasError;
    }
}
