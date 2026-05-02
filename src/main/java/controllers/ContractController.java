package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.util.StringConverter;
import models.Contract;
import models.Partner;
import services.ContractService;
import services.PartnerService;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Date;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class ContractController {

    @FXML private ComboBox<Partner> cmbPartner;
    @FXML private TextField txtTitle;
    @FXML private TextArea txtDescription;
    @FXML private TextField txtValue;
    @FXML private Label lblEurValue;
    @FXML private ComboBox<String> cmbStatus;
    @FXML private DatePicker dpStartDate;
    @FXML private DatePicker dpEndDate;
    @FXML private Label lblDuration;
    @FXML private TextField txtSearch;

    @FXML private TableView<Contract> tableContracts;
    @FXML private TableColumn<Contract, Integer> colId;
    @FXML private TableColumn<Contract, Integer> colPartnerId;
    @FXML private TableColumn<Contract, String> colTitle;
    @FXML private TableColumn<Contract, Date> colStartDate;
    @FXML private TableColumn<Contract, Date> colEndDate;
    @FXML private TableColumn<Contract, Double> colValue;
    @FXML private TableColumn<Contract, String> colStatus;

    private ContractService contractService;
    private PartnerService partnerService;
    private ObservableList<Contract> contractList;
    private Contract selectedContract;

    @FXML
    public void initialize() {
        contractService = new ContractService();
        partnerService = new PartnerService();

        setupComboBoxes();
        setupTableColumns();
        setupListeners();
        
        refreshTable();
    }

    private void setupComboBoxes() {
        // Load Status
        cmbStatus.setItems(FXCollections.observableArrayList("Actif", "Expiré", "En attente"));

        // Load Partners from DB
        ObservableList<Partner> partners = FXCollections.observableArrayList(partnerService.getAll());
        cmbPartner.setItems(partners);

        // Tell ComboBox how to display Partner objects
        cmbPartner.setConverter(new StringConverter<Partner>() {
            @Override
            public String toString(Partner partner) {
                return partner == null ? null : partner.getName();
            }

            @Override
            public Partner fromString(String string) {
                return null; // Not needed
            }
        });
    }

    private void setupTableColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colPartnerId.setCellValueFactory(new PropertyValueFactory<>("partnerId"));
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colStartDate.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        colEndDate.setCellValueFactory(new PropertyValueFactory<>("endDate"));
        colValue.setCellValueFactory(new PropertyValueFactory<>("value"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
    }

    private void setupListeners() {
        // Date listeners for automatic calculation
        dpStartDate.valueProperty().addListener((obs, oldVal, newVal) -> calculateDuration());
        dpEndDate.valueProperty().addListener((obs, oldVal, newVal) -> calculateDuration());

        // Table selection listener
        tableContracts.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectedContract = newSelection;
                
                // Select matching partner in combo box
                for (Partner p : cmbPartner.getItems()) {
                    if (p.getId() == selectedContract.getPartnerId()) {
                        cmbPartner.setValue(p);
                        break;
                    }
                }
                
                txtTitle.setText(selectedContract.getTitle());
                txtDescription.setText(selectedContract.getDescription());
                txtValue.setText(String.valueOf(selectedContract.getValue()));
                cmbStatus.setValue(selectedContract.getStatus());
                
                if (selectedContract.getStartDate() != null)
                    dpStartDate.setValue(selectedContract.getStartDate().toLocalDate());
                if (selectedContract.getEndDate() != null)
                    dpEndDate.setValue(selectedContract.getEndDate().toLocalDate());
            }
        });
    }

    private void calculateDuration() {
        if (dpStartDate.getValue() != null && dpEndDate.getValue() != null) {
            long days = ChronoUnit.DAYS.between(dpStartDate.getValue(), dpEndDate.getValue());
            if (days < 0) {
                lblDuration.setText("Erreur: Dates inversées!");
                lblDuration.setStyle("-fx-text-fill: red;");
            } else {
                lblDuration.setText(days + " jours");
                lblDuration.setStyle("-fx-text-fill: green;");
            }
        } else {
            lblDuration.setText("0 jours");
            lblDuration.setStyle("-fx-text-fill: #2980b9;");
        }
    }

    private void refreshTable() {
        contractList = FXCollections.observableArrayList(contractService.getAll());
        
        FilteredList<Contract> filteredData = new FilteredList<>(contractList, b -> true);
        
        txtSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(contract -> {
                if (newValue == null || newValue.isEmpty()) return true;
                return contract.getTitle().toLowerCase().contains(newValue.toLowerCase());
            });
        });
        
        SortedList<Contract> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(tableContracts.comparatorProperty());
        tableContracts.setItems(sortedData);
    }

    @FXML
    private void handleConvertCurrency() {
        String valStr = txtValue.getText();
        if (valStr == null || valStr.trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Erreur", "Veuillez saisir une valeur en TND d'abord.");
            return;
        }
        try {
            double tndValue = Double.parseDouble(valStr);
            if (lblEurValue != null) lblEurValue.setText("...");
            
            new Thread(() -> {
                try {
                    URL url = new URL("https://api.exchangerate-api.com/v4/latest/TND");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();
                    
                    String json = response.toString();
                    double rate = 0;
                    int eurIndex = json.indexOf("\"EUR\":");
                    if (eurIndex != -1) {
                        int commaIndex = json.indexOf(",", eurIndex);
                        if (commaIndex == -1) commaIndex = json.indexOf("}", eurIndex);
                        String rateStr = json.substring(eurIndex + 6, commaIndex).trim();
                        rate = Double.parseDouble(rateStr);
                    }
                    
                    final double finalRate = rate;
                    javafx.application.Platform.runLater(() -> {
                        if (finalRate > 0) {
                            double eurValue = tndValue * finalRate;
                            if (lblEurValue != null) lblEurValue.setText(String.format("%.2f €", eurValue));
                        } else {
                            if (lblEurValue != null) lblEurValue.setText("Erreur API");
                        }
                    });

                } catch (Exception e) {
                    javafx.application.Platform.runLater(() -> {
                        if (lblEurValue != null) lblEurValue.setText("Erreur réseau");
                    });
                }
            }).start();
            
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Valeur invalide.");
        }
    }

    @FXML
    void handleAdd(ActionEvent event) {
        if (isInputValid()) {
            Contract c = new Contract(
                    cmbPartner.getValue().getId(),
                    txtTitle.getText().trim(),
                    txtDescription.getText().trim(),
                    Date.valueOf(dpStartDate.getValue()),
                    Date.valueOf(dpEndDate.getValue()),
                    Double.parseDouble(txtValue.getText().trim()),
                    cmbStatus.getValue()
            );
            contractService.add(c);
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Contrat ajouté!");
            refreshTable();
            clearForm();
        }
    }

    @FXML
    void handleUpdate(ActionEvent event) {
        if (selectedContract != null && isInputValid()) {
            selectedContract.setPartnerId(cmbPartner.getValue().getId());
            selectedContract.setTitle(txtTitle.getText().trim());
            selectedContract.setDescription(txtDescription.getText().trim());
            selectedContract.setStartDate(Date.valueOf(dpStartDate.getValue()));
            selectedContract.setEndDate(Date.valueOf(dpEndDate.getValue()));
            selectedContract.setValue(Double.parseDouble(txtValue.getText().trim()));
            selectedContract.setStatus(cmbStatus.getValue());

            contractService.update(selectedContract);
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Contrat modifié!");
            refreshTable();
            clearForm();
        } else if (selectedContract == null) {
            showAlert(Alert.AlertType.WARNING, "Erreur", "Veuillez sélectionner un contrat.");
        }
    }

    @FXML
    void handleDelete(ActionEvent event) {
        if (selectedContract != null) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Supprimer?", ButtonType.YES, ButtonType.NO);
            confirm.showAndWait();
            if (confirm.getResult() == ButtonType.YES) {
                contractService.delete(selectedContract);
                refreshTable();
                clearForm();
            }
        } else {
            showAlert(Alert.AlertType.WARNING, "Erreur", "Veuillez sélectionner un contrat.");
        }
    }

    @FXML
    void handleClear(ActionEvent event) {
        clearForm();
    }

    @FXML
    void handleExportCSV(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Enregistrer les contrats");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers CSV", "*.csv"));
        File file = fileChooser.showSaveDialog(tableContracts.getScene().getWindow());

        if (file != null) {
            try (PrintWriter writer = new PrintWriter(file)) {
                writer.println("ID,PartenaireID,Titre,DateDebut,DateFin,Valeur,Statut");
                for (Contract c : tableContracts.getItems()) {
                    writer.printf("%d,%d,%s,%s,%s,%.2f,%s\n", 
                            c.getId(), c.getPartnerId(), c.getTitle(), c.getStartDate(), c.getEndDate(), c.getValue(), c.getStatus());
                }
                showAlert(Alert.AlertType.INFORMATION, "Export", "Fichier exporté avec succès!");
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
            }
        }
    }

    private void clearForm() {
        cmbPartner.getSelectionModel().clearSelection();
        txtTitle.clear();
        txtDescription.clear();
        txtValue.clear();
        cmbStatus.getSelectionModel().clearSelection();
        dpStartDate.setValue(null);
        dpEndDate.setValue(null);
        txtSearch.clear();
        selectedContract = null;
        tableContracts.getSelectionModel().clearSelection();
        lblDuration.setText("0 jours");
    }

    private boolean isInputValid() {
        if (cmbPartner.getValue() == null || txtTitle.getText().isEmpty() || cmbStatus.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Erreur", "Veuillez remplir tous les champs obligatoires.");
            return false;
        }

        try {
            Double.parseDouble(txtValue.getText());
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING, "Erreur", "La valeur doit être un nombre valide.");
            return false;
        }

        if (dpStartDate.getValue() == null || dpEndDate.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Erreur", "Les dates sont obligatoires.");
            return false;
        }

        if (dpEndDate.getValue().isBefore(dpStartDate.getValue())) {
            showAlert(Alert.AlertType.WARNING, "Erreur", "La date de fin ne peut pas précéder la date de début.");
            return false;
        }

        return true;
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
