package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import services.OffreService;

import java.util.Map;

public class TopDonorsController {

    @FXML
    private BarChart<String, Number> barChart;
    @FXML
    private TableView<DonorStat> statTable;
    @FXML
    private TableColumn<DonorStat, Integer> rankCol;
    @FXML
    private TableColumn<DonorStat, String> phoneCol;
    @FXML
    private TableColumn<DonorStat, Integer> totalCol;

    private OffreService offreService = new OffreService();

    @FXML
    public void initialize() {
        rankCol.setCellValueFactory(new PropertyValueFactory<>("rank"));
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("phone"));
        totalCol.setCellValueFactory(new PropertyValueFactory<>("total"));

        loadData();
    }

    private void loadData() {
        Map<String, Integer> stats = offreService.getDonorsStatistics();
        
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Quantité d'articles donnés");

        ObservableList<DonorStat> list = FXCollections.observableArrayList();
        int rank = 1;
        for (Map.Entry<String, Integer> entry : stats.entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
            list.add(new DonorStat(rank++, entry.getKey(), entry.getValue()));
        }

        barChart.getData().clear();
        barChart.getData().add(series);
        statTable.setItems(list);
    }

    public static class DonorStat {
        private int rank;
        private String phone;
        private int total;

        public DonorStat(int rank, String phone, int total) {
            this.rank = rank;
            this.phone = phone;
            this.total = total;
        }

        public int getRank() { return rank; }
        public String getPhone() { return phone; }
        public int getTotal() { return total; }
    }
}
