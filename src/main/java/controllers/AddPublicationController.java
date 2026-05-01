package controllers;

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
import services.ServicePublication;

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

    private String selectedImagePath = "";
    private ServicePublication sp = new ServicePublication();

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
    private void savePublication(ActionEvent event) {
        String titre = txtTitre.getText().trim();
        String contenu = txtContenu.getText().trim();

        if (titre.isEmpty() || contenu.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur de Saisie", "Le titre et le contenu sont obligatoires.");
            return;
        }

        if (titre.length() < 5) {
            showAlert(Alert.AlertType.WARNING, "Titre trop court", "Le titre doit contenir au moins 5 caractÃ¢â€Å“Ã‚Â¿res.");
            return;
        }

        if (contenu.length() < 15) {
            showAlert(Alert.AlertType.WARNING, "Contenu trop court",
                    "Le contenu doit contenir au moins 15 caractÃ¢â€Å“Ã‚Â¿res pour Ã¢â€Å“Ã‚Â¬tre significatif.");
            return;
        }

        Publication p = new Publication();
        p.setTitre(titre);
        p.setContenu(contenu);
        p.setType(comboType.getValue());
        p.setAuteurId(utils.UserSession.getInstance().getUser().getId());
        p.setImagePath(selectedImagePath);

        sp.add(p);

        // Redirect back to forum list
        goBack(event);
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void goBack(ActionEvent event) {
        utils.NavigationService.switchScene(event, "/esprit/tn/fxml/forum.fxml", "Forum");
    }
}