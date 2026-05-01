package controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import models.PanierItem;
import utils.PanierSession;

public class PanierController {

    @FXML private TableView<PanierItem> panierTable;
    @FXML private TableColumn<PanierItem, String> produitCol;
    @FXML private TableColumn<PanierItem, Integer> quantiteCol;
    @FXML private TableColumn<PanierItem, String> prixCol;
    @FXML private TableColumn<PanierItem, String> totalCol;
    @FXML private Label totalLabel;
    @FXML private Label emptyLabel;

    private Runnable onCheckoutCompleted;

    @FXML
    public void initialize() {
        produitCol.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getProduit().getNom()));
        quantiteCol.setCellValueFactory(new PropertyValueFactory<>("quantite"));
        prixCol.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(
                String.format("%.2f DT", cell.getValue().getProduit().getPrix())));
        totalCol.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(
                String.format("%.2f DT", cell.getValue().getSousTotal().doubleValue())));
        refresh();
    }

    public void setOnCheckoutCompleted(Runnable onCheckoutCompleted) {
        this.onCheckoutCompleted = onCheckoutCompleted;
    }

    private void refresh() {
        panierTable.setItems(FXCollections.observableArrayList(PanierSession.getInstance().getItems()));
        totalLabel.setText(String.format("%.2f DT", PanierSession.getInstance().getTotal().doubleValue()));
        boolean empty = PanierSession.getInstance().isEmpty();
        emptyLabel.setVisible(empty);
        emptyLabel.setManaged(empty);
    }

    @FXML
    private void handleRemove() {
        PanierItem item = panierTable.getSelectionModel().getSelectedItem();
        if (item == null) {
            return;
        }
        PanierSession.getInstance().removeProduit(item.getProduit().getId());
        refresh();
    }

    @FXML
    private void handleCommander() {
        if (PanierSession.getInstance().isEmpty()) {
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/esprit/tn/fxml/commande_checkout.fxml"));
            Parent root = loader.load();
            CommandeCheckoutController controller = loader.getController();
            controller.setOnOrderCreated(() -> {
                refresh();
                if (onCheckoutCompleted != null) {
                    onCheckoutCompleted.run();
                }
                Stage panierStage = (Stage) panierTable.getScene().getWindow();
                panierStage.close();
            });

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Finaliser la commande");
            stage.setScene(new Scene(root, 760, 620));
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) panierTable.getScene().getWindow();
        stage.close();
    }
}
