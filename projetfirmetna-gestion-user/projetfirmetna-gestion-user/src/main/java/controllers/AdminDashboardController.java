package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import services.UserService;
import services.CommandeService;
import services.ProduitService;
import models.User;
import models.Commande;
import models.Produit;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class AdminDashboardController {

    @FXML private Label totalUsersLabel;
    @FXML private Label activeUsersLabel;
    @FXML private Label totalProductsLabel;
    @FXML private Label totalCommandesLabel;
    @FXML private Label validatedCommandesLabel;
    @FXML private Label totalRevenueLabel;
    
    @FXML private Label agriculteurCountLabel;
    @FXML private Label clientCountLabel;
    @FXML private Label donateurCountLabel;

    @FXML private Label vegetauxCountLabel;
    @FXML private Label animauxCountLabel;
    @FXML private Label bioCountLabel;
    @FXML private Label totalStockLabel;

    @FXML private PieChart userDistributionChart;
    @FXML private PieChart productDistributionChart;
    @FXML private AreaChart<String, Number> activityChart;
    
    @FXML private ProgressBar agriProgress;
    @FXML private ProgressBar clientProgress;
    @FXML private ProgressBar donateurProgress;

    private UserService userService = new UserService();
    private CommandeService commandeService = new CommandeService();
    private ProduitService produitService = new ProduitService();

    @FXML
    public void initialize() {
        try {
            loadStatistics();
        } catch (Exception e) {
            System.err.println("Error initializing dashboard: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadStatistics() {
        List<User> allUsers = userService.getAll();
        if (allUsers == null) allUsers = new ArrayList<>();
        
        if (totalUsersLabel != null) totalUsersLabel.setText(String.valueOf(allUsers.size()));
        
        long activeCount = allUsers.stream()
                .filter(u -> u != null && !"Inactif".equalsIgnoreCase(u.getStatus()))
                .count();
        if (activeUsersLabel != null) activeUsersLabel.setText(String.valueOf(activeCount));

        Map<String, Long> roleCounts = allUsers.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(user -> {
                    String role = user.getRole();
                    return role != null ? role : "Autre";
                }, Collectors.counting()));

        long agriculteurs = roleCounts.getOrDefault("Agriculteur", 0L);
        long clients = roleCounts.getOrDefault("Client", 0L);
        long donateurs = roleCounts.getOrDefault("Donateur", 0L);

        if (agriculteurCountLabel != null) agriculteurCountLabel.setText(String.valueOf(agriculteurs));
        if (clientCountLabel != null) clientCountLabel.setText(String.valueOf(clients));
        if (donateurCountLabel != null) donateurCountLabel.setText(String.valueOf(donateurs));

        if (userDistributionChart != null) {
            ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList(
                    new PieChart.Data("Agriculteurs", agriculteurs),
                    new PieChart.Data("Clients", clients),
                    new PieChart.Data("Donateurs", donateurs)
            );
            userDistributionChart.setData(pieData);
        }

        if (!allUsers.isEmpty()) {
            double total = (double) allUsers.size();
            if (agriProgress != null) agriProgress.setProgress(agriculteurs / total);
            if (clientProgress != null) clientProgress.setProgress(clients / total);
            if (donateurProgress != null) donateurProgress.setProgress(donateurs / total);
        }

        setupActivityChart(allUsers);
        loadProductStatistics();
        loadCommandeStatistics();
    }

    private void setupActivityChart(List<User> allUsers) {
        if (activityChart == null) return;
        
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Nouveaux Utilisateurs");

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM");
        
        Map<String, Long> dailyRegistrations = allUsers.stream()
                .filter(u -> u != null && u.getRegistrationDate() != null)
                .sorted(Comparator.comparing(User::getRegistrationDate))
                .collect(Collectors.groupingBy(
                        u -> sdf.format(u.getRegistrationDate()),
                        LinkedHashMap::new,
                        Collectors.counting()
                ));

        if (dailyRegistrations.isEmpty()) {
            series.getData().add(new XYChart.Data<>("01/04", 1));
            series.getData().add(new XYChart.Data<>("10/04", Math.max(1, allUsers.size())));
        } else {
            dailyRegistrations.forEach((day, count) -> {
                series.getData().add(new XYChart.Data<>(day, count));
            });
        }

        activityChart.getData().clear();
        activityChart.getData().add(series);
    }

    private void loadProductStatistics() {
        List<Produit> allProducts = produitService.getAll();
        if (allProducts == null) allProducts = new ArrayList<>();

        int totalProducts = allProducts.size();
        if (totalProductsLabel != null) totalProductsLabel.setText(String.valueOf(totalProducts));

        long vegetaux = allProducts.stream()
                .filter(p -> p != null && "vegetale".equalsIgnoreCase(p.getType()))
                .count();
        long animaux = allProducts.stream()
                .filter(p -> p != null && "animale".equalsIgnoreCase(p.getType()))
                .count();
        long bio = allProducts.stream()
                .filter(p -> p != null && p.isBio())
                .count();
        int totalStock = allProducts.stream()
                .mapToInt(p -> p != null ? p.getStock() : 0)
                .sum();

        if (vegetauxCountLabel != null) vegetauxCountLabel.setText(String.valueOf(vegetaux));
        if (animauxCountLabel != null) animauxCountLabel.setText(String.valueOf(animaux));
        if (bioCountLabel != null) bioCountLabel.setText(String.valueOf(bio));
        if (totalStockLabel != null) totalStockLabel.setText(String.valueOf(totalStock));

        if (productDistributionChart != null) {
            ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList(
                    new PieChart.Data("Vegetaux", vegetaux),
                    new PieChart.Data("Animaux", animaux)
            );
            productDistributionChart.setData(pieData);
        }
    }

    private void loadCommandeStatistics() {
        List<Commande> allCommandes = commandeService.getAll();
        if (allCommandes == null) allCommandes = new ArrayList<>();

        int totalCommandes = allCommandes.size();
        if (totalCommandesLabel != null) totalCommandesLabel.setText(String.valueOf(totalCommandes));

        long validatedCommandes = allCommandes.stream()
                .filter(c -> c != null && "confirmée".equalsIgnoreCase(c.getStatut()))
                .count();
        if (validatedCommandesLabel != null) validatedCommandesLabel.setText(String.valueOf(validatedCommandes));

        java.math.BigDecimal totalRevenue = allCommandes.stream()
                .filter(c -> c != null && c.getTotal() != null)
                .map(Commande::getTotal)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

        if (totalRevenueLabel != null) totalRevenueLabel.setText(String.format("%.2f TND", totalRevenue));
    }
}
