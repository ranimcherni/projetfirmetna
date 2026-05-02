package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import models.Lieu;
import services.LieuService;
import java.io.File;
import javafx.scene.web.WebView;
import javafx.scene.web.WebEngine;
import javafx.concurrent.Worker;
import netscape.javascript.JSObject;

public class LieuFormController {

    @FXML private Label titleLabel;
    @FXML private Label subtitleLabel;
    @FXML private TextField villeField;
    @FXML private TextField adresseField;
    @FXML private TextField capaciteField;
    @FXML private TextField imageField;
    @FXML private CheckBox disponibiliteCheck;
    @FXML private TextArea descriptionArea;
    @FXML private Button saveBtn;
    @FXML private WebView mapView;

    @FXML private Label villeError;
    @FXML private Label adresseError;
    @FXML private Label capaciteError;
    @FXML private Label descriptionError;

    private final LieuService lieuService = new LieuService();
    private Lieu current;
    private boolean isEdit = false;

    @FXML
    public void initialize() {
        resetErrors();
        setupMap();
    }

    private void setupMap() {
        if (mapView != null) {
            WebEngine webEngine = mapView.getEngine();
            webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
                if (newState == Worker.State.SUCCEEDED) {
                    JSObject window = (JSObject) webEngine.executeScript("window");
                    window.setMember("javaConnector", new JavaConnector());
                }
            });
            try {
                String mapUrl = getClass().getResource("/esprit/tn/html/map.html").toExternalForm();
                webEngine.load(mapUrl);
            } catch (Exception e) {
                System.err.println("Erreur chargement map: " + e.getMessage());
            }
        }
    }

    public class JavaConnector {
        public void updateAddress(String address) {
            javafx.application.Platform.runLater(() -> {
                adresseField.setText(address);
                hide(adresseError);
            });
        }
    }

    private void resetErrors() {
        hide(villeError);
        hide(adresseError);
        hide(capaciteError);
        hide(descriptionError);
    }

    private void hide(Label l) {
        if (l != null) {
            l.setVisible(false);
            l.setManaged(false);
        }
    }

    private void show(Label l, String msg) {
        if (l != null) {
            l.setText(msg);
            l.setVisible(true);
            l.setManaged(true);
        }
    }

    public void setLieu(Lieu lieu) {
        this.current = lieu;
        this.isEdit = true;
        titleLabel.setText("Modifier le lieu #" + lieu.getId());
        subtitleLabel.setText("Mettez à jour les informations du lieu.");
        saveBtn.setText("✔ Mettre à jour");

        villeField.setText(lieu.getVille());
        adresseField.setText(lieu.getAdresse());
        capaciteField.setText(String.valueOf(lieu.getCapacite()));
        imageField.setText(lieu.getImage() != null ? lieu.getImage() : "");
        disponibiliteCheck.setSelected(lieu.isDisponibilite());
        descriptionArea.setText(lieu.getDescription() != null ? lieu.getDescription() : "");
    }

    @FXML
    private void handleSave() {
        if (!validate()) {
            return;
        }
        if (!isEdit) {
            current = new Lieu();
        }
        current.setVille(villeField.getText().trim());
        current.setAdresse(adresseField.getText().trim());
        current.setCapacite(Integer.parseInt(capaciteField.getText().trim()));
        current.setImage(imageField.getText() != null && !imageField.getText().trim().isEmpty()
                ? imageField.getText().trim() : null);
        current.setDisponibilite(disponibiliteCheck.isSelected());
        current.setDescription(descriptionArea.getText() != null ? descriptionArea.getText().trim() : null);

        if (!isEdit) {
            lieuService.add(current);
        } else {
            lieuService.update(current);
        }
        goBack();
    }

    @FXML
    private void handleCancel() {
        goBack();
    }

    @FXML
    private void handleUploadImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        File file = fileChooser.showOpenDialog(imageField.getScene().getWindow());
        if (file != null) {
            imageField.setText(file.toURI().toString());
        }
    }

    private void goBack() {
        AdminLayoutController.getInstance().loadView("/esprit/tn/fxml/admin_lieux.fxml");
    }

    private boolean validate() {
        resetErrors();
        boolean ok = true;

        String ville = villeField.getText().trim();
        if (ville.isEmpty()) {
            show(villeError, "Ce champ est requis");
            ok = false;
        }

        String adresse = adresseField.getText().trim();
        if (adresse.isEmpty()) {
            show(adresseError, "Ce champ est requis");
            ok = false;
        }

        String cap = capaciteField.getText().trim();
        if (cap.isEmpty()) {
            show(capaciteError, "Ce champ est requis");
            ok = false;
        } else {
            try {
                int c = Integer.parseInt(cap);
                if (c <= 0) {
                    show(capaciteError, "La capacité doit être > 0");
                    ok = false;
                }
            } catch (NumberFormatException e) {
                show(capaciteError, "Nombre entier invalide");
                ok = false;
            }
        }

        return ok;
    }
}
