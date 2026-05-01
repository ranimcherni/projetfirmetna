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
import services.AIRecommendationService;
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
    @FXML private VBox publicSection;
    @FXML private TextField txtLoginEmail;
    @FXML private Label lblError;
    @FXML private TableView<Partner> tablePublicPartners;
    @FXML private TableColumn<Partner, String> colPublicName;
    @FXML private TableColumn<Partner, String> colPublicType;
    @FXML private TableColumn<Partner, String> colPublicAddress;

    // Dashboard Section
    @FXML private VBox dashboardSection;
    @FXML private Label lblPartnerName;
    @FXML private Label lblPartnerInfo;
    @FXML private Label lblWeather;

    // Table
    @FXML private TableView<Contract> tableContracts;
    @FXML private TableColumn<Contract, String> colTitle;
    @FXML private TableColumn<Contract, Double> colValue;
    @FXML private TableColumn<Contract, Date> colStartDate;
    @FXML private TableColumn<Contract, Date> colEndDate;
    @FXML private TableColumn<Contract, String> colStatus;
    @FXML private TableColumn<Contract, String> colTimeLeft;

    // AI Recommendations
    @FXML private VBox aiContainer;
    @FXML private Label lblAiRec1;
    @FXML private Label lblAiRec2;
    @FXML private Label lblAiRec3;

    private PartnerService partnerService;
    private ContractService contractService;
    private AIRecommendationService aiService;
    private Partner currentPartner;

    @FXML
    public void initialize() {
        partnerService = new PartnerService();
        contractService = new ContractService();
        aiService = new AIRecommendationService();

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
        
        // Load AI Recommendations
        loadAIRecommendations();
    }

    private void loadAIRecommendations() {
        List<Partner> allPartners = partnerService.getAll();
        List<Partner> recs = aiService.getRecommendations(currentPartner, allPartners);

        Label[] labels = {lblAiRec1, lblAiRec2, lblAiRec3};
        for (int i = 0; i < labels.length; i++) {
            if (i < recs.size()) {
                Partner p = recs.get(i);
                labels[i].setText("✨ " + p.getName() + " (" + p.getType() + ") - " + extractCity(p.getAddress()));
                labels[i].setVisible(true);
            } else {
                labels[i].setVisible(false);
            }
        }
    }

    private String extractCity(String address) {
        if (address == null || address.isEmpty()) return "N/A";
        if (address.contains(",")) return address.split(",")[0].trim();
        return address.trim();
    }

    private void fetchWeather() {
        lblWeather.setText("Loading..."); 
        new Thread(() -> {
            try {
                // Using HTTPS with a robust approach
                URL url = new URL("https://api.open-meteo.com/v1/forecast?latitude=36.8065&longitude=10.1815&current_weather=true");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("User-Agent", "Mozilla/5.0");
                conn.setConnectTimeout(10000);
                
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                
                String json = response.toString();
                // Regex to find temperature in current_weather block
                java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\"temperature\":\\s*([0-9.-]+)");
                java.util.regex.Matcher matcher = pattern.matcher(json);
                
                String tempStr = "";
                if (matcher.find()) {
                    tempStr = matcher.group(1);
                }

                final String finalTemp = tempStr;
                javafx.application.Platform.runLater(() -> {
                    if (!finalTemp.isEmpty()) {
                        lblWeather.setText(finalTemp + " °C");
                    } else {
                        lblWeather.setText("N/A");
                    }
                });
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    lblWeather.setText("Offline");
                });
            }
        }).start();
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
        NavigationService.switchScene(event, "/esprit/tn/fxml/home.fxml", "Bienvenue");
    }
}
