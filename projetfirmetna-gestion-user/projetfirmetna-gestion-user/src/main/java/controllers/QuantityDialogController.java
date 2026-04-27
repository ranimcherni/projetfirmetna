package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.stage.Stage;
import models.Produit;

public class QuantityDialogController {

    @FXML private Label productNameLabel;
    @FXML private Label stockLabel;
    @FXML private Spinner<Integer> quantitySpinner;

    private Produit produit;
    private Runnable onConfirm;

    @FXML
    public void initialize() {
        // Spinner will be configured when produit is set
    }

    public void setProduit(Produit produit, Runnable onConfirm) {
        this.produit = produit;
        this.onConfirm = onConfirm;

        productNameLabel.setText(produit.getNom());
        stockLabel.setText("Stock disponible: " + produit.getStock());

        // Set spinner range: 1 to stock
        int max = Math.max(1, produit.getStock());
        SpinnerValueFactory<Integer> valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, max, 1);
        quantitySpinner.setValueFactory(valueFactory);
    }

    @FXML
    private void handleConfirm() {
        if (onConfirm != null) {
            onConfirm.run();
        }
        closeDialog();
    }

    @FXML
    private void handleCancel() {
        closeDialog();
    }

    private void closeDialog() {
        Stage stage = (Stage) productNameLabel.getScene().getWindow();
        stage.close();
    }

    public int getSelectedQuantity() {
        return quantitySpinner.getValue();
    }
}