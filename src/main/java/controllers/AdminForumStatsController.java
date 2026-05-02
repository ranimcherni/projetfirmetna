package controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import services.ForumStatsService;

import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;

public class AdminForumStatsController implements Initializable {

    @FXML private Label lblTotalPubs;
    @FXML private Label lblTotalComments;
    @FXML private Label lblTotalReactions;
    @FXML private BarChart<String, Number> barChartUsers;
    @FXML private PieChart pieChartActivity;

    private final ForumStatsService statsService = new ForumStatsService();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadStats();
    }

    @FXML
    private void refreshStats() {
        loadStats();
    }

    private void loadStats() {
        // 1. Load Summary Numbers
        Map<String, Integer> typeStats = statsService.getInteractionTypeStats();
        int pubs = typeStats.getOrDefault("Publications", 0);
        int comments = typeStats.getOrDefault("Commentaires", 0);
        int likes = typeStats.getOrDefault("Likes", 0);
        int dislikes = typeStats.getOrDefault("Dislikes", 0);
        int totalReactions = likes + dislikes;

        lblTotalPubs.setText(String.valueOf(pubs));
        lblTotalComments.setText(String.valueOf(comments));
        lblTotalReactions.setText(String.valueOf(totalReactions));

        // 2. Populate Pie Chart
        pieChartActivity.getData().clear();
        pieChartActivity.getData().add(new PieChart.Data("Publications", pubs));
        pieChartActivity.getData().add(new PieChart.Data("Commentaires", comments));
        pieChartActivity.getData().add(new PieChart.Data("Likes", likes));
        pieChartActivity.getData().add(new PieChart.Data("Dislikes", dislikes));

        // 3. Populate Bar Chart
        barChartUsers.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Interactions");

        Map<String, Integer> userStats = statsService.getUserInteractionStats();
        userStats.forEach((name, count) -> {
            series.getData().add(new XYChart.Data<>(name, count));
        });

        barChartUsers.getData().add(series);
    }
}
