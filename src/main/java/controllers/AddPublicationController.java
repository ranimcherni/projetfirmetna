package controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import models.Publication;
import models.User;
import services.HateSpeechService;
import services.ServicePublication;
import services.UserService;
import utils.NavigationService;
import utils.UserSession;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class AddPublicationController implements Initializable {

    @FXML
    private TextField txtTitre;
    @FXML
    private TextArea txtContenu;
    @FXML
    private ComboBox<String> comboType;
    @FXML
    private Label labelImage;
    @FXML
    private Label labelPdf;

    @FXML
    private Button btnSave;          // tie to the Save button in FXML (optional but nice)

    private String selectedImagePath = "";
    private String selectedPdfPath = "";
    private ServicePublication sp   = new ServicePublication();
    private HateSpeechService hss   = new HateSpeechService();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        comboType.getItems().addAll("Discussion", "Question", "Annonce");
        comboType.setValue("Discussion");
    }

    @FXML
    private void chooseImage(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.gif"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            selectedImagePath = file.getAbsolutePath();
            labelImage.setText(file.getName());
        }
    }

    @FXML
    private void choosePdf(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir un document PDF");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Documents PDF", "*.pdf"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            selectedPdfPath = file.getAbsolutePath();
            labelPdf.setText(file.getName());
        }
    }

    @FXML
    private void savePublication(ActionEvent event) {
        String titre   = txtTitre.getText().trim();
        String contenu = txtContenu.getText().trim();

        if (titre.isEmpty() || contenu.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur de Saisie", "Le titre et le contenu sont obligatoires.");
            return;
        }
        if (titre.length() < 5) {
            showAlert(Alert.AlertType.WARNING, "Titre trop court", "Le titre doit contenir au moins 5 caractères.");
            return;
        }
        if (contenu.length() < 15) {
            showAlert(Alert.AlertType.WARNING, "Contenu trop court",
                    "Le contenu doit contenir au moins 15 caractères pour être significatif.");
            return;
        }

        // Disable button to prevent double-submit
        if (btnSave != null) btnSave.setDisable(true);

        // Build the publication object now (on FX thread)
        Publication p = new Publication();
        p.setTitre(titre);
        p.setContenu(contenu);
        p.setType(comboType.getValue());
        User currentUser = UserSession.getInstance().getUser();
        int userId = (currentUser != null) ? currentUser.getId() : 1;
        p.setAuteurId(userId);
        p.setImagePath(selectedImagePath);
        p.setPdfPath(selectedPdfPath);

        // Run hate-speech check in background so UI doesn't freeze
        String textToCheck = titre + " " + contenu;
        new Thread(() -> {
            HateSpeechService.Result result = hss.analyze(textToCheck);
            Platform.runLater(() -> {
                if (btnSave != null) btnSave.setDisable(false);
                if (result.toxic) {
                    String reason = result.reason.isEmpty() ? "contenu inapproprié"
                            : result.reason.replace("_", " ").toLowerCase();
                    showAlert(Alert.AlertType.ERROR,
                            "🚫 Publication bloquée",
                            "Votre publication a été détectée comme contenant du discours haineux "
                            + "(" + reason + ", score: " + String.format("%.0f", result.score * 100) + "%)."
                            + "\n\nVeuillez réviser votre contenu avant de publier.");
                } else {
                    try {
                        sp.add(p);
                        // Gamification: Increment user actions for forum post
                        new UserService().incrementActionsCount(userId);
                        showAlert(Alert.AlertType.INFORMATION, "✅ Succès", "Publication ajoutée avec succès !");
                        goBack(event);
                    } catch (Exception e) {
                        showAlert(Alert.AlertType.ERROR, "Échec",
                                "Impossible d'ajouter la publication : " + e.getMessage());
                    }
                }
            });
        }).start();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void handleProduits(ActionEvent event) {
        utils.NavigationService.switchScene(event, "/esprit/tn/fxml/product_marketplace.fxml", "Produits");
    }

    @FXML
    private void handleProduitsVegetaux(ActionEvent event) {
        utils.ProductNavigationState.setSelectedType("vegetale");
        utils.NavigationService.switchScene(event, "/esprit/tn/fxml/product_marketplace.fxml", "Marketplace - Vegetaux");
    }

    @FXML
    private void handleProduitsAnimaux(ActionEvent event) {
        utils.ProductNavigationState.setSelectedType("animale");
        utils.NavigationService.switchScene(event, "/esprit/tn/fxml/product_marketplace.fxml", "Marketplace - Animaux");
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        utils.UserSession.getInstance().cleanUserSession();
        utils.NavigationService.switchScene(event, "/esprit/tn/fxml/home.fxml", "Bienvenue");
    }

    @FXML
    private void handleProfil(ActionEvent event) {
        utils.NavigationService.switchScene(event, "/esprit/tn/fxml/profile.fxml", "Mon Profil");
    }

    @FXML
    private void handleEvenements(javafx.event.ActionEvent event) {
        utils.NavigationService.switchScene(event, "/esprit/tn/fxml/front_evenements.fxml", "Événements");
    }

    @FXML
    private void handleDons(javafx.event.ActionEvent event) {
        utils.NavigationService.switchScene(event, "/esprit/tn/fxml/admin_donations_offres.fxml", "Dons");
    }

    @FXML
    private void handleAccueil(ActionEvent event) {
        utils.NavigationService.switchScene(event, "/esprit/tn/fxml/front.fxml", "Accueil");
    }

    @FXML
    private void goBack(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/esprit/tn/fxml/forum.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 900, 600));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
