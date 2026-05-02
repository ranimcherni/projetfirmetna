package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import models.Partner;
import services.PartnerService;

import java.io.File;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.sql.Timestamp;
import java.util.regex.Pattern;

public class PartnerController {

    @FXML
    private TextField txtName;
    @FXML
    private ComboBox<String> cmbType;
    @FXML
    private TextField txtEmail;
    @FXML
    private TextField txtPhone;
    @FXML
    private TextArea txtAddress;
    @FXML
    private TextField txtSearch;

    @FXML
    private TableView<Partner> tablePartners;
    @FXML
    private TableColumn<Partner, Integer> colId;
    @FXML
    private TableColumn<Partner, String> colName;
    @FXML
    private TableColumn<Partner, String> colType;
    @FXML
    private TableColumn<Partner, String> colEmail;
    @FXML
    private TableColumn<Partner, String> colPhone;
    @FXML
    private TableColumn<Partner, Timestamp> colCreatedAt;

    private PartnerService partnerService;
    private ObservableList<Partner> partnerList;
    private Partner selectedPartner;

    @FXML
    public void initialize() {
        partnerService = new PartnerService();

        // Initialize ComboBox
        cmbType.setItems(FXCollections.observableArrayList("Supplier", "Distributor", "Donor"));

        // Set up columns in the table
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        colCreatedAt.setCellValueFactory(new PropertyValueFactory<>("createdAt"));

        // Load data into table and setup search
        refreshTable();

        // Listen for selection changes
        tablePartners.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectedPartner = newSelection;
                txtName.setText(selectedPartner.getName());
                cmbType.setValue(selectedPartner.getType());
                txtEmail.setText(selectedPartner.getEmail());
                txtPhone.setText(selectedPartner.getPhone());
                txtAddress.setText(selectedPartner.getAddress());
            }
        });
    }

    private void refreshTable() {
        partnerList = FXCollections.observableArrayList(partnerService.getAll());
        
        // Setup Search/Filter
        FilteredList<Partner> filteredData = new FilteredList<>(partnerList, b -> true);
        
        txtSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(partner -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                String lowerCaseFilter = newValue.toLowerCase();
                
                if (partner.getName().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (partner.getEmail() != null && partner.getEmail().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }
                return false;
            });
        });
        
        SortedList<Partner> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(tablePartners.comparatorProperty());
        tablePartners.setItems(sortedData);
    }

    @FXML
    void handleAdd(ActionEvent event) {
        if (isInputValid(0)) { // 0 means new record
            Partner p = new Partner(
                    txtName.getText().trim(),
                    cmbType.getValue(),
                    txtEmail.getText().trim(),
                    txtPhone.getText().trim(),
                    txtAddress.getText().trim()
            );
            partnerService.add(p);
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Partenaire ajouté avec succès!");
            refreshTable();
            clearForm();
        }
    }

    @FXML
    void handleUpdate(ActionEvent event) {
        if (selectedPartner != null) {
            if (isInputValid(selectedPartner.getId())) {
                selectedPartner.setName(txtName.getText().trim());
                selectedPartner.setType(cmbType.getValue());
                selectedPartner.setEmail(txtEmail.getText().trim());
                selectedPartner.setPhone(txtPhone.getText().trim());
                selectedPartner.setAddress(txtAddress.getText().trim());

                partnerService.update(selectedPartner);
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Partenaire modifié avec succès!");
                refreshTable();
                clearForm();
            }
        } else {
            showAlert(Alert.AlertType.WARNING, "Erreur de sélection", "Veuillez sélectionner un partenaire à modifier.");
        }
    }

    @FXML
    void handleDelete(ActionEvent event) {
        if (selectedPartner != null) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Êtes-vous sûr de vouloir supprimer ce partenaire?", ButtonType.YES, ButtonType.NO);
            confirm.showAndWait();
            
            if (confirm.getResult() == ButtonType.YES) {
                partnerService.delete(selectedPartner);
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Partenaire supprimé avec succès!");
                refreshTable();
                clearForm();
            }
        } else {
            showAlert(Alert.AlertType.WARNING, "Erreur de sélection", "Veuillez sélectionner un partenaire à supprimer.");
        }
    }

    @FXML
    void handleClear(ActionEvent event) {
        clearForm();
    }
    
    @FXML
    void handleExportCSV(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Enregistrer les partenaires");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers CSV", "*.csv"));
        File file = fileChooser.showSaveDialog(tablePartners.getScene().getWindow());

        if (file != null) {
            try (PrintWriter writer = new PrintWriter(file)) {
                writer.println("ID,Nom,Type,Email,Telephone,Date_Creation"); // Header
                for (Partner p : tablePartners.getItems()) {
                    writer.printf("%d,%s,%s,%s,%s,%s\n", 
                            p.getId(), p.getName(), p.getType(), p.getEmail(), p.getPhone(), p.getCreatedAt());
                }
                showAlert(Alert.AlertType.INFORMATION, "Export Réussi", "Fichier CSV exporté avec succès!");
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Erreur d'export", "Impossible de sauvegarder le fichier: " + e.getMessage());
            }
        }
    }

    @FXML
    void handleGenerateQR(ActionEvent event) {
        if (selectedPartner != null) {
            try {
                String data = "Nom: " + selectedPartner.getName() + "\n" +
                              "Email: " + selectedPartner.getEmail() + "\n" +
                              "Tel: " + selectedPartner.getPhone();
                
                String encodedData = URLEncoder.encode(data, "UTF-8");
                String apiUrl = "https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=" + encodedData;

                Image qrImage = new Image(apiUrl);
                ImageView imageView = new ImageView(qrImage);

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("QR Code Partenaire");
                alert.setHeaderText("Fiche de " + selectedPartner.getName());
                
                VBox dialogPaneContent = new VBox();
                dialogPaneContent.setAlignment(javafx.geometry.Pos.CENTER);
                dialogPaneContent.getChildren().add(imageView);
                
                alert.getDialogPane().setContent(dialogPaneContent);
                alert.showAndWait();

            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de générer le QR Code.");
            }
        } else {
            showAlert(Alert.AlertType.WARNING, "Erreur", "Veuillez sélectionner un partenaire d'abord.");
        }
    }

    private void clearForm() {
        txtName.clear();
        cmbType.getSelectionModel().clearSelection();
        txtEmail.clear();
        txtPhone.clear();
        txtAddress.clear();
        txtSearch.clear();
        selectedPartner = null;
        tablePartners.getSelectionModel().clearSelection();
    }

    private boolean isInputValid(int currentId) {
        String name = txtName.getText();
        String type = cmbType.getValue();
        String email = txtEmail.getText();
        String phone = txtPhone.getText();

        if (name == null || name.trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Erreur de Validation", "Le nom est obligatoire.");
            return false;
        }
        if (type == null) {
            showAlert(Alert.AlertType.WARNING, "Erreur de Validation", "Le type est obligatoire.");
            return false;
        }
        
        // Email Regex Validation
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        if (email != null && !email.trim().isEmpty() && !Pattern.compile(emailRegex).matcher(email).matches()) {
            showAlert(Alert.AlertType.WARNING, "Erreur de Validation", "Format d'email invalide.");
            return false;
        }
        
        // Phone Validation (Numbers only, e.g., 8 digits for Tunisia)
        if (phone != null && !phone.trim().isEmpty() && !phone.matches("\\d{8,}")) {
            showAlert(Alert.AlertType.WARNING, "Erreur de Validation", "Le téléphone doit contenir au moins 8 chiffres.");
            return false;
        }
        
        // Uniqueness test
        if (!partnerService.isEmailOrPhoneUnique(email, phone, currentId)) {
            showAlert(Alert.AlertType.WARNING, "Erreur d'Unicité", "Un partenaire avec cet email ou ce téléphone existe déjà!");
            return false;
        }

        return true;
    }

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
