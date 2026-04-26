package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import models.Offre;
import services.OffreService;
import java.util.Map;

public class AdminDonationsOffresController {

    @FXML
    private TableView<Offre> offreTable;
    @FXML
    private TableColumn<Offre, Integer> colId;
    @FXML
    private TableColumn<Offre, String> colTelephone;
    @FXML
    private TableColumn<Offre, String> colCategorie;
    @FXML
    private TableColumn<Offre, String> colDescription;
    @FXML
    private TableColumn<Offre, Integer> colQuantite;

    @FXML
    private TextField telephoneField;
    @FXML
    private TextField categorieField;
    @FXML
    private TextArea descriptionField;
    @FXML
    private TextField photoField;
    @FXML
    private TextField quantiteField;
    @FXML
    private javafx.scene.control.Label errorTelephone;
    @FXML
    private javafx.scene.control.Label errorCategorie;
    @FXML
    private javafx.scene.control.Label errorPhoto;
    @FXML
    private javafx.scene.control.Label errorQuantite;
    @FXML
    private javafx.scene.control.Label errorDescription;
    @FXML
    private TextField searchField;
    @FXML
    private CheckBox stockOnlyCheck;
    @FXML
    private ComboBox<String> sortCombo;

    private final OffreService offreService = new OffreService();
    private final ObservableList<Offre> masterData = FXCollections.observableArrayList();
    private FilteredList<Offre> filteredData;

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colTelephone.setCellValueFactory(new PropertyValueFactory<>("telephone"));
        colCategorie.setCellValueFactory(new PropertyValueFactory<>("categorie"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colQuantite.setCellValueFactory(new PropertyValueFactory<>("quantite"));

        setupFilterAndSort();
        refreshTable();

        offreTable.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, selected) -> {
            if (selected != null) {
                telephoneField.setText(selected.getTelephone());
                categorieField.setText(selected.getCategorie());
                descriptionField.setText(selected.getDescription());
                photoField.setText(selected.getPhoto());
                quantiteField.setText(String.valueOf(selected.getQuantite()));
            }
        });
    }

    private void setupFilterAndSort() {
        sortCombo.setItems(FXCollections.observableArrayList(
                "Plus recent",
                "Quantite croissante",
                "Quantite decroissante",
                "Produit A-Z"
        ));
        sortCombo.getSelectionModel().selectFirst();

        filteredData = new FilteredList<>(masterData, item -> true);
        SortedList<Offre> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(offreTable.comparatorProperty());
        offreTable.setItems(sortedData);

        searchField.textProperty().addListener((obs, oldValue, newValue) -> applyFilters());
        stockOnlyCheck.selectedProperty().addListener((obs, oldValue, newValue) -> applyFilters());
        sortCombo.valueProperty().addListener((obs, oldValue, newValue) -> applyFilters());
    }

    private void applyFilters() {
        String keyword = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();
        boolean stockOnly = stockOnlyCheck.isSelected();

        filteredData.setPredicate(offre -> {
            boolean matchesText = keyword.isEmpty()
                    || safe(offre.getCategorie()).toLowerCase().contains(keyword)
                    || safe(offre.getDescription()).toLowerCase().contains(keyword)
                    || safe(offre.getTelephone()).toLowerCase().contains(keyword);
            boolean matchesStock = !stockOnly || offre.getQuantite() > 0;
            return matchesText && matchesStock;
        });

        String sort = sortCombo.getValue();
        if ("Quantite croissante".equals(sort)) {
            filteredData.getSource().sort((a, b) -> Integer.compare(a.getQuantite(), b.getQuantite()));
        } else if ("Quantite decroissante".equals(sort)) {
            filteredData.getSource().sort((a, b) -> Integer.compare(b.getQuantite(), a.getQuantite()));
        } else if ("Produit A-Z".equals(sort)) {
            filteredData.getSource().sort((a, b) -> safe(a.getCategorie()).compareToIgnoreCase(safe(b.getCategorie())));
        } else {
            filteredData.getSource().sort((a, b) -> Integer.compare(b.getId(), a.getId()));
        }
    }

    @FXML
    private void handleAdd() {
        if (!validateInput()) return;
        Integer quantite = Integer.parseInt(quantiteField.getText().trim());

        Offre offre = new Offre(
                telephoneField.getText().trim(),
                categorieField.getText().trim(),
                descriptionField.getText().trim(),
                photoField.getText().trim(),
                quantite
        );
        offreService.add(offre);
        refreshTable();
        clearForm();
    }

    @FXML
    private void handleUpdate() {
        Offre selected = offreTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Veuillez selectionner une offre a modifier.");
            return;
        }
        if (!validateInput()) return;
        Integer quantite = Integer.parseInt(quantiteField.getText().trim());

        selected.setTelephone(telephoneField.getText().trim());
        selected.setCategorie(categorieField.getText().trim());
        selected.setDescription(descriptionField.getText().trim());
        selected.setPhoto(photoField.getText().trim());
        selected.setQuantite(quantite);

        offreService.update(selected);
        refreshTable();
    }

    @FXML
    private void handleDelete() {
        Offre selected = offreTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Veuillez selectionner une offre a supprimer.");
            return;
        }
        offreService.delete(selected);
        refreshTable();
        clearForm();
    }

    @FXML
    private void handleClear() {
        clearForm();
        offreTable.getSelectionModel().clearSelection();
    }

    @FXML
    private void handleStatistics() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/esprit/tn/fxml/top_donors.fxml"));
            javafx.scene.Parent root = loader.load();
            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle("Top Donateurs");
            stage.setScene(new javafx.scene.Scene(root));
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.show();
        } catch (java.io.IOException e) {
            e.printStackTrace();
            showError("Impossible de charger l'interface des Top Donateurs : " + e.getMessage());
        }
    }

    @FXML
    private void handleOpenChatbot() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/esprit/tn/fxml/chatbot.fxml"));
            javafx.scene.Parent root = loader.load();
            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle("Assistant Intelligent");
            stage.setScene(new javafx.scene.Scene(root));
            stage.initModality(javafx.stage.Modality.NONE);
            stage.show();
        } catch (java.io.IOException e) {
            e.printStackTrace();
            showError("Impossible de charger le Chatbot : " + e.getMessage());
        }
    }

    private void refreshTable() {
        masterData.setAll(offreService.getAll());
        applyFilters();
    }

    private void clearForm() {
        telephoneField.clear();
        categorieField.clear();
        descriptionField.clear();
        photoField.clear();
        quantiteField.clear();
        
        if (errorTelephone != null) {
            errorTelephone.setVisible(false);
            errorTelephone.setManaged(false);
            errorCategorie.setVisible(false);
            errorCategorie.setManaged(false);
            errorPhoto.setVisible(false);
            errorPhoto.setManaged(false);
            errorQuantite.setVisible(false);
            errorQuantite.setManaged(false);
            errorDescription.setVisible(false);
            errorDescription.setManaged(false);
        }
    }

    private boolean validateInput() {
        boolean isValid = true;
        
        if (telephoneField.getText().trim().isEmpty() || !telephoneField.getText().trim().matches("\\d{8}")) {
            errorTelephone.setText("Téléphone invalide (8 chiffres)");
            errorTelephone.setVisible(true);
            errorTelephone.setManaged(true);
            isValid = false;
        } else {
            errorTelephone.setVisible(false);
            errorTelephone.setManaged(false);
        }

        if (categorieField.getText().trim().isEmpty()) {
            errorCategorie.setText("Catégorie requise");
            errorCategorie.setVisible(true);
            errorCategorie.setManaged(true);
            isValid = false;
        } else {
            errorCategorie.setVisible(false);
            errorCategorie.setManaged(false);
        }

        if (photoField.getText().trim().isEmpty()) {
            errorPhoto.setText("Photo requise");
            errorPhoto.setVisible(true);
            errorPhoto.setManaged(true);
            isValid = false;
        } else {
            errorPhoto.setVisible(false);
            errorPhoto.setManaged(false);
        }

        try {
            int qte = Integer.parseInt(quantiteField.getText().trim());
            if (qte <= 0) {
                errorQuantite.setText("Quantité doit être > 0");
                errorQuantite.setVisible(true);
                errorQuantite.setManaged(true);
                isValid = false;
            } else {
                errorQuantite.setVisible(false);
                errorQuantite.setManaged(false);
            }
        } catch (NumberFormatException e) {
            errorQuantite.setText("Quantité invalide");
            errorQuantite.setVisible(true);
            errorQuantite.setManaged(true);
            isValid = false;
        }

        if (descriptionField.getText().trim().isEmpty()) {
            errorDescription.setText("Description requise");
            errorDescription.setVisible(true);
            errorDescription.setManaged(true);
            isValid = false;
        } else {
            errorDescription.setVisible(false);
            errorDescription.setManaged(false);
        }

        return isValid;
    }

    private Integer parseQuantite() {
        try {
            int qte = Integer.parseInt(quantiteField.getText().trim());
            if (qte <= 0) {
                showError("La quantite doit etre > 0.");
                return null;
            }
            return qte;
        } catch (Exception e) {
            showError("Quantite invalide.");
            return null;
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText("Operation impossible");
        alert.setContentText(message);
        alert.showAndWait();
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
