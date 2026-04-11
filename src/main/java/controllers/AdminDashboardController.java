package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import services.UserService;
import models.User;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class AdminDashboardController {

    @FXML private Label totalUsersLabel;
    @FXML private Label activeUsersLabel;
    @FXML private Label totalProductsLabel;
    @FXML private Label totalEventsLabel;
    
    @FXML private Label agriculteurCountLabel;
    @FXML private Label clientCountLabel;
    @FXML private Label donateurCountLabel;

    @FXML private PieChart userDistributionChart;
    @FXML private AreaChart<String, Number> activityChart;

    private UserService userService = new UserService();

    @FXML
    public void initialize() {
        loadStatistics();
    }

    private void loadStatistics() {
        List<User> allUsers = userService.getAll();
        
        // Basic Stats
        totalUsersLabel.setText(String.valueOf(allUsers.size()));
        
        long activeCount = allUsers.stream()
                .filter(u -> !"Inactif".equalsIgnoreCase(u.getStatus()))
                .count();
        activeUsersLabel.setText(String.valueOf(activeCount));

        // Group by Role
        Map<String, Long> roleCounts = allUsers.stream()
                .collect(Collectors.groupingBy(User::getRole, Collectors.counting()));

        long agriculteurs = roleCounts.getOrDefault("Agriculteur", 0L);
        long clients = roleCounts.getOrDefault("Client", 0L);
        long donateurs = roleCounts.getOrDefault("Donateur", 0L);

        agriculteurCountLabel.setText(String.valueOf(agriculteurs));
        clientCountLabel.setText(String.valueOf(clients));
        donateurCountLabel.setText(String.valueOf(donateurs));

        // Populate PieChart
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList(
                new PieChart.Data("Agriculteurs", agriculteurs),
                new PieChart.Data("Clients", clients),
                new PieChart.Data("Donateurs", donateurs)
        );
        userDistributionChart.setData(pieData);

        // Populate Activity Chart (Registration Growth)
        setupActivityChart(allUsers);

        // Placeholders for other modules (until their services are connected)
        totalProductsLabel.setText("14");
        totalEventsLabel.setText("4");
    }

    private void setupActivityChart(List<User> allUsers) {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Nouveaux Utilisateurs");

        // Sort users by registration date and group by day
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM");
        
        Map<String, Long> dailyRegistrations = allUsers.stream()
                .filter(u -> u.getRegistrationDate() != null)
                .sorted(Comparator.comparing(User::getRegistrationDate))
                .collect(Collectors.groupingBy(
                        u -> sdf.format(u.getRegistrationDate()),
                        LinkedHashMap::new,
                        Collectors.counting()
                ));

        // If no registration dates found, add dummy data for visual testing
        if (dailyRegistrations.isEmpty()) {
            series.getData().add(new XYChart.Data<>("01/04", 1));
            series.getData().add(new XYChart.Data<>("03/04", 3));
            series.getData().add(new XYChart.Data<>("05/04", 2));
            series.getData().add(new XYChart.Data<>("08/04", 5));
            series.getData().add(new XYChart.Data<>("10/04", allUsers.size()));
        } else {
            dailyRegistrations.forEach((day, count) -> {
                series.getData().add(new XYChart.Data<>(day, count));
            });
        }

        activityChart.getData().clear();
        activityChart.getData().add(series);
    }
}
