package controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.util.StringConverter;
import models.Evenement;
import models.Lieu;
import java.io.File;
import services.EvenementService;
import services.LieuService;

import java.sql.Date;
import java.time.LocalDate;

public class EvenementFormController {

    @FXML private Label titleLabel;
    @FXML private Label subtitleLabel;
    @FXML private ComboBox<Lieu> lieuCombo;
    @FXML private TextField nomField;
    @FXML private DatePicker datePicker;
    @FXML private TextField organisateurField;
    @FXML private TextField imageField;
    @FXML private TextArea descriptionArea;
    @FXML private Button saveBtn;

    @FXML private Label lieuError;
    @FXML private Label nomError;
    @FXML private Label dateError;
    @FXML private Label orgError;
    @FXML private Label descError;

    private final LieuService lieuService = new LieuService();
    private final EvenementService evenementService = new EvenementService();
    private Evenement current;
    private boolean isEdit = false;

    @FXML
    public void initialize() {
        lieuCombo.setItems(FXCollections.observableArrayList(lieuService.getAll()));
        lieuCombo.setConverter(new StringConverter<Lieu>() {
            @Override
            public String toString(Lieu lieu) {
                if (lieu == null) {
                    return null;
                }
                return lieu.getVille() + " — " + lieu.getAdresse();
            }

            @Override
            public Lieu fromString(String string) {
                return null;
            }
        });
        resetErrors();
    }

    private void resetErrors() {
        hide(lieuError);
        hide(nomError);
        hide(dateError);
        hide(orgError);
        hide(descError);
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

    public void setEvenement(Evenement ev) {
        this.current = ev;
        this.isEdit = true;
        titleLabel.setText("Modifier l'événement #" + ev.getId());
        subtitleLabel.setText("Mettez à jour les informations.");
        saveBtn.setText("✔ Mettre à jour");

        nomField.setText(ev.getNom());
        organisateurField.setText(ev.getOrganisateur() != null ? ev.getOrganisateur() : "");
        imageField.setText(ev.getImage() != null ? ev.getImage() : "");
        descriptionArea.setText(ev.getDescription() != null ? ev.getDescription() : "");
        if (ev.getDateEvenement() != null) {
            datePicker.setValue(ev.getDateEvenement().toLocalDate());
        }

        for (Lieu l : lieuCombo.getItems()) {
            if (l.getId() == ev.getLieuId()) {
                lieuCombo.getSelectionModel().select(l);
                break;
            }
        }
    }

    @FXML
    private void handleSave() {
        if (!validateForm()) {
            return;
        }
        if (!isEdit) {
            current = new Evenement();
        }

        Lieu selected = lieuCombo.getSelectionModel().getSelectedItem();
        current.setLieuId(selected.getId());
        current.setNom(nomField.getText().trim());
        current.setDescription(descriptionArea.getText() != null ? descriptionArea.getText().trim() : null);
        current.setOrganisateur(organisateurField.getText() != null && !organisateurField.getText().trim().isEmpty()
                ? organisateurField.getText().trim() : null);
        current.setImage(imageField.getText() != null && !imageField.getText().trim().isEmpty()
                ? imageField.getText().trim() : null);

        LocalDate ld = datePicker.getValue();
        current.setDateEvenement(Date.valueOf(ld));

        if (!isEdit) {
            evenementService.add(current);
        } else {
            evenementService.update(current);
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
        fileChooser.setTitle("Choisir l'image de l'événement");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        File file = fileChooser.showOpenDialog(imageField.getScene().getWindow());
        if (file != null) {
            imageField.setText(file.toURI().toString());
        }
    }

    private void goBack() {
        AdminLayoutController.getInstance().loadView("/esprit/tn/fxml/admin_evenements.fxml");
    }

    private boolean validateForm() {
        resetErrors();
        boolean isValid = true;

        // Validation Lieu
        if (lieuCombo.getSelectionModel().getSelectedItem() == null) {
            show(lieuError, "Le lieu est obligatoire");
            isValid = false;
        }

        // Validation Nom
        if (nomField.getText().trim().isEmpty()) {
            show(nomError, "Le nom est obligatoire");
            isValid = false;
        }

        // Validation Date
        LocalDate date = datePicker.getValue();
        if (date == null) {
            show(dateError, "La date est obligatoire");
            isValid = false;
        } else if (date.isBefore(LocalDate.now())) {
            show(dateError, "La date ne doit pas être dans le passé");
            isValid = false;
        }

        // Validation Organisateur
        if (organisateurField.getText().trim().isEmpty()) {
            show(orgError, "L'organisateur est obligatoire");
            isValid = false;
        }

        // Validation Description
        if (descriptionArea.getText().trim().isEmpty()) {
            show(descError, "La description est obligatoire");
            isValid = false;
        }

        return isValid;
    }
}
