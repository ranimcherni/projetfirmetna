package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import services.UserService;
import models.User;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class AdminDashboardController {

    @FXML private Label totalUsersLabel;
    @FXML private Label activeUsersLabel;
    @FXML private Label totalProductsLabel;
    
    @FXML private Label agriculteurCountLabel;
    @FXML private Label clientCountLabel;
    @FXML private Label donateurCountLabel;

    @FXML private PieChart userDistributionChart;
    @FXML private AreaChart<String, Number> activityChart;
    
    @FXML private ProgressBar agriProgress;
    @FXML private ProgressBar clientProgress;
    @FXML private ProgressBar donateurProgress;

    private UserService userService = new UserService();

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

        if (totalProductsLabel != null) totalProductsLabel.setText("14");
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
}
