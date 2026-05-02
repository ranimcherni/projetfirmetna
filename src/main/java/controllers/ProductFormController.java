package controllers;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import models.Produit;
import services.ProduitService;
import utils.AlertUtils;

import java.io.File;

public class ProductFormController {

    @FXML private Label titleLabel;
    @FXML private Label subtitleLabel;
    @FXML private TextField nomField;
    @FXML private TextArea descriptionArea;
    @FXML private TextField prixField;
    @FXML private ComboBox<String> typeCombo;
    @FXML private TextField uniteField;
    @FXML private TextField stockField;
    @FXML private CheckBox bioCheck;
    @FXML private TextField badgeField;
    @FXML private TextField imageUrlField;
    @FXML private Button saveBtn;

    @FXML private Label nomError;
    @FXML private Label prixError;
    @FXML private Label uniteError;
    @FXML private Label stockError;
    @FXML private Label imageError;

    private final ProduitService produitService = new ProduitService();
    private Produit currentProduit;
    private boolean editMode;
    private boolean modalMode;
    private Runnable onSaved;

    @FXML
    public void initialize() {
        typeCombo.setItems(FXCollections.observableArrayList("vegetale", "animale"));
        typeCombo.getSelectionModel().select("vegetale");
        resetErrors();
    }

    public void configureForAdmin(Runnable onSaved) {
        this.onSaved = onSaved;
        this.modalMode = false;
    }

    public void configureForMarketplace(Runnable onSaved) {
        this.onSaved = onSaved;
        this.modalMode = true;
        subtitleLabel.setText("Ajoutez un produit a vendre avec image, categorie et stock.");
    }

    public void setProduit(Produit produit) {
        this.currentProduit = produit;
        this.editMode = true;
        titleLabel.setText("Modifier le produit #" + produit.getId());
        subtitleLabel.setText("Mettez a jour les informations du produit.");
        saveBtn.setText("Mettre a jour");

        nomField.setText(produit.getNom());
        descriptionArea.setText(produit.getDescription());
        prixField.setText(String.valueOf(produit.getPrix()));
        typeCombo.setValue(produit.getType());
        uniteField.setText(produit.getUnite());
        stockField.setText(String.valueOf(produit.getStock()));
        bioCheck.setSelected(produit.isBio());
        badgeField.setText(produit.getBadge());
        imageUrlField.setText(produit.getImageUrl());
    }

    @FXML
    private void handleChooseImage(ActionEvent event) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choisir une image produit");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.webp"));

        File file = chooser.showOpenDialog(((Button) event.getSource()).getScene().getWindow());
        if (file != null) {
            imageUrlField.setText(file.toURI().toString());
        }
    }

    @FXML
    private void handleSave() {
        if (!isValid()) {
            return;
        }

        if (currentProduit == null) {
            currentProduit = new Produit();
        }

        currentProduit.setNom(nomField.getText().trim());
        currentProduit.setDescription(descriptionArea.getText().trim());
        currentProduit.setPrix(Double.parseDouble(prixField.getText().trim()));
        currentProduit.setType(typeCombo.getValue());
        currentProduit.setUnite(uniteField.getText().trim());
        currentProduit.setStock(Integer.parseInt(stockField.getText().trim()));
        currentProduit.setBio(bioCheck.isSelected());
        currentProduit.setBadge(badgeField.getText().trim().isEmpty() ? "Nouveau" : badgeField.getText().trim());
        currentProduit.setImageUrl(imageUrlField.getText().trim());

        if (editMode) {
            produitService.update(currentProduit);
        } else {
            produitService.add(currentProduit);
        }

        AlertUtils.showSuccess("Produit", editMode ? "Produit mis a jour." : "Produit ajoute.");
        if (onSaved != null) {
            onSaved.run();
        }
        closeOrReturn();
    }

    @FXML
    private void handleCancel() {
        closeOrReturn();
    }

    private void closeOrReturn() {
        if (modalMode) {
            Stage stage = (Stage) saveBtn.getScene().getWindow();
            stage.close();
        } else {
            AdminLayoutController.getInstance().loadView("/esprit/tn/fxml/admin_products.fxml");
        }
    }

    private boolean isValid() {
        resetErrors();
        boolean hasError = false;

        if (nomField.getText().trim().isEmpty()) {
            showError(nomError, "Le nom est obligatoire.");
            hasError = true;
        }

        try {
            double prix = Double.parseDouble(prixField.getText().trim());
            if (prix <= 0) {
                showError(prixError, "Le prix doit etre positif.");
                hasError = true;
            }
        } catch (Exception e) {
            showError(prixError, "Prix invalide.");
            hasError = true;
        }

        if (uniteField.getText().trim().isEmpty()) {
            showError(uniteError, "L'unite est obligatoire.");
            hasError = true;
        }

        try {
            int stock = Integer.parseInt(stockField.getText().trim());
            if (stock < 0) {
                showError(stockError, "Le stock ne peut pas etre negatif.");
                hasError = true;
            }
        } catch (Exception e) {
            showError(stockError, "Stock invalide.");
            hasError = true;
        }

        if (imageUrlField.getText().trim().isEmpty()) {
            showError(imageError, "Une image est obligatoire.");
            hasError = true;
        }

        return !hasError;
    }

    private void resetErrors() {
        hideError(nomError);
        hideError(prixError);
        hideError(uniteError);
        hideError(stockError);
        hideError(imageError);
    }

    private void showError(Label label, String message) {
        label.setText(message);
        label.setVisible(true);
        label.setManaged(true);
    }

    private void hideError(Label label) {
        label.setVisible(false);
        label.setManaged(false);
    }
}
