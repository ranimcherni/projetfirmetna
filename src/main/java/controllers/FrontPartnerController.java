package controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import models.Contract;
import models.Partner;
import services.ContractService;
import services.PartnerService;
import utils.NavigationService;

import java.sql.Date;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class FrontPartnerController {

    // Public Section
    @FXML private javafx.scene.control.ScrollPane publicSection;
    @FXML private TextField txtLoginEmail;
    @FXML private Label lblError;
    @FXML private TableView<Partner> tablePublicPartners;
    @FXML private TableColumn<Partner, String> colPublicName;
    @FXML private TableColumn<Partner, String> colPublicType;
    @FXML private TableColumn<Partner, String> colPublicAddress;

    // Dashboard Section
    @FXML private javafx.scene.control.ScrollPane dashboardSection;
    @FXML private Label lblPartnerName;
    @FXML private Label lblPartnerInfo;
    @FXML private Label lblWeather;
    @FXML private VBox vboxAIInsights;

    // Table
    @FXML private TableView<Contract> tableContracts;
    @FXML private TableColumn<Contract, String> colTitle;
    @FXML private TableColumn<Contract, Double> colValue;
    @FXML private TableColumn<Contract, Date> colStartDate;
    @FXML private TableColumn<Contract, Date> colEndDate;
    @FXML private TableColumn<Contract, String> colStatus;
    @FXML private TableColumn<Contract, String> colTimeLeft;

    private PartnerService partnerService;
    private ContractService contractService;
    private Partner currentPartner;

    @FXML
    public void initialize() {
        partnerService = new PartnerService();
        contractService = new ContractService();

        // Initially show public, hide dashboard
        publicSection.setVisible(true);
        dashboardSection.setVisible(false);

        setupTable();
        loadPublicPartners();
    }

    private void loadPublicPartners() {
        colPublicName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colPublicType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colPublicAddress.setCellValueFactory(new PropertyValueFactory<>("address"));

        List<Partner> allPartners = partnerService.getAll();
        tablePublicPartners.setItems(FXCollections.observableArrayList(allPartners));
    }

    private void setupTable() {
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colValue.setCellValueFactory(new PropertyValueFactory<>("value"));
        colStartDate.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        colEndDate.setCellValueFactory(new PropertyValueFactory<>("endDate"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Custom cell value factory for dynamically calculating time left
        colTimeLeft.setCellValueFactory(cellData -> {
            Contract contract = cellData.getValue();
            if (contract.getEndDate() == null) return new SimpleStringProperty("Non défini");

            LocalDate endDate = contract.getEndDate().toLocalDate();
            LocalDate today = LocalDate.now();

            long daysLeft = ChronoUnit.DAYS.between(today, endDate);

            if (daysLeft < 0) {
                return new SimpleStringProperty("Expiré depuis " + Math.abs(daysLeft) + " jours");
            } else if (daysLeft == 0) {
                return new SimpleStringProperty("Expire aujourd'hui !");
            } else {
                return new SimpleStringProperty(daysLeft + " jours restants");
            }
        });
    }

    @FXML
    void handleLogin(ActionEvent event) {
        String email = txtLoginEmail.getText().trim();
        if (email.isEmpty()) {
            lblError.setText("Veuillez entrer votre email.");
            lblError.setVisible(true);
            return;
        }

        currentPartner = partnerService.getPartnerByEmail(email);

        if (currentPartner != null) {
            // Login Success
            lblError.setVisible(false);
            showDashboard();
        } else {
            // Login Failed
            lblError.setText("Aucun partenaire trouvé avec cet email.");
            lblError.setVisible(true);
        }
    }

    private void showDashboard() {
        // Hide public, show dashboard
        publicSection.setVisible(false);
        dashboardSection.setVisible(true);

        // Set Partner Info
        lblPartnerName.setText(currentPartner.getName());
        lblPartnerInfo.setText(currentPartner.getType() + " | " + currentPartner.getEmail() + " | " + currentPartner.getPhone());

        // Load Contracts
        List<Contract> contracts = contractService.getContractsByPartnerId(currentPartner.getId());
        ObservableList<Contract> observableContracts = FXCollections.observableArrayList(contracts);
        tableContracts.setItems(observableContracts);
        
        // Fetch Weather API
        fetchWeather();
        loadAIInsights();
    }

    private void loadAIInsights() {
        vboxAIInsights.getChildren().clear();
        
        List<Partner> allPartners = partnerService.getAll();
        if (allPartners == null || currentPartner == null) return;

        // Simple Recommendation Logic:
        // 1. Filter out the current partner
        // 2. Prioritize different types (e.g., Supplier wants Distributor)
        // 3. Prioritize same city
        
        List<Partner> recommendations = allPartners.stream()
            .filter(p -> p.getId() != currentPartner.getId())
            .sorted((p1, p2) -> {
                int score1 = 0;
                int score2 = 0;
                
                // Bonus for different type
                if (!p1.getType().equals(currentPartner.getType())) score1 += 10;
                if (!p2.getType().equals(currentPartner.getType())) score2 += 10;
                
                // Bonus for same city
                if (p1.getAddress() != null && currentPartner.getAddress() != null &&
                    p1.getAddress().toLowerCase().contains(currentPartner.getAddress().toLowerCase())) score1 += 5;
                if (p2.getAddress() != null && currentPartner.getAddress() != null &&
                    p2.getAddress().toLowerCase().contains(currentPartner.getAddress().toLowerCase())) score2 += 5;
                
                return Integer.compare(score2, score1); // Higher score first
            })
            .limit(3)
            .toList();

        if (recommendations.isEmpty()) {
            Label noMatch = new Label("✦ Aucun partenaire trouvé pour le moment.");
            noMatch.setStyle("-fx-text-fill: rgba(255,255,255,0.7); -fx-font-style: italic;");
            vboxAIInsights.getChildren().add(noMatch);
        } else {
            for (Partner p : recommendations) {
                Label rec = new Label("✦ " + p.getName() + " (" + p.getType() + ") - " + p.getAddress());
                rec.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
                vboxAIInsights.getChildren().add(rec);
            }
        }
    }

    private void fetchWeather() {
        new Thread(() -> {
            try {
                URL url = new URL("https://api.open-meteo.com/v1/forecast?latitude=36.8065&longitude=10.1815&current_weather=true");
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
                String tempStr = "";
                // Look specifically for the temperature value inside "current_weather":{...}
                int currentBox = json.indexOf("\"current_weather\":");
                if (currentBox != -1) {
                    int tempIndex = json.indexOf("\"temperature\":", currentBox);
                    if (tempIndex != -1) {
                        int commaIndex = json.indexOf(",", tempIndex);
                        if (commaIndex == -1) commaIndex = json.indexOf("}", tempIndex);
                        tempStr = json.substring(tempIndex + 14, commaIndex).trim();
                        // Remove any quotes if present (unlikely for numbers but safe)
                        tempStr = tempStr.replace("\"", "");
                    }
                }
                final String finalTemp = tempStr;
                javafx.application.Platform.runLater(() -> {
                    if (!finalTemp.isEmpty()) {
                        lblWeather.setText(finalTemp + " \u00B0C");
                    } else {
                        lblWeather.setText("Indisponible");
                    }
                });
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> lblWeather.setText("Erreur API"));
            }
        }).start();
    }

    @FXML
    void handleAccueil(ActionEvent event) {
        NavigationService.switchScene(event, "/esprit/tn/fxml/front.fxml", "Accueil");
    }

    @FXML
    void handleProduits(ActionEvent event) {
        NavigationService.switchScene(event, "/esprit/tn/fxml/product_marketplace.fxml", "Marketplace");
    }

    @FXML
    void handleProduitsVegetaux(ActionEvent event) {
        utils.ProductNavigationState.setSelectedType("vegetale");
        NavigationService.switchScene(event, "/esprit/tn/fxml/product_marketplace.fxml", "Marketplace - Vegetaux");
    }

    @FXML
    void handleProduitsAnimaux(ActionEvent event) {
        utils.ProductNavigationState.setSelectedType("animale");
        NavigationService.switchScene(event, "/esprit/tn/fxml/product_marketplace.fxml", "Marketplace - Animaux");
    }

    @FXML
    void handleEvenements(ActionEvent event) {
        NavigationService.switchScene(event, "/esprit/tn/fxml/front_evenements.fxml", "Événements");
    }

    @FXML
    void handleForum(ActionEvent event) {
        NavigationService.switchScene(event, "/esprit/tn/fxml/forum.fxml", "Forum - Firmetna");
    }

    @FXML
    void handleDons(ActionEvent event) {
        NavigationService.switchScene(event, "/esprit/tn/fxml/front_donations_offres.fxml", "Donations & Solidarité");
    }

    @FXML
    void handlePartenariats(ActionEvent event) {
        // Already here, do nothing or refresh
    }

    @FXML
    void handleLogout(ActionEvent event) {
        currentPartner = null;
        dashboardSection.setVisible(false);
        publicSection.setVisible(true);
        txtLoginEmail.clear();
    }

    @FXML
    void handleGoBack(ActionEvent event) {
        NavigationService.switchScene(event, "/esprit/tn/fxml/front.fxml", "Accueil");
    }
}
