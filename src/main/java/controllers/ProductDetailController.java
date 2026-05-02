package controllers;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import models.Produit;

public class ProductDetailController {

    @FXML private VBox rootBox;
    @FXML private ImageView productImageView;
    @FXML private Label nomLabel;
    @FXML private Label typeLabel;
    @FXML private Label prixLabel;
    @FXML private Label uniteLabel;
    @FXML private Label stockLabel;
    @FXML private Label badgeLabel;
    @FXML private Label bioLabel;
    @FXML private Label descriptionLabel;
    @FXML private Button addToCartBtn;

    private Runnable addToCartAction;

    @FXML
    public void initialize() {
        rootBox.setOpacity(0);
        rootBox.setScaleX(0.92);
        rootBox.setScaleY(0.92);

        FadeTransition fade = new FadeTransition(Duration.millis(220), rootBox);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();

        ScaleTransition scale = new ScaleTransition(Duration.millis(240), rootBox);
        scale.setFromX(0.92);
        scale.setFromY(0.92);
        scale.setToX(1);
        scale.setToY(1);
        scale.play();
    }

    public void setProduit(Produit produit, Runnable addToCartAction) {
        this.addToCartAction = addToCartAction;
        nomLabel.setText(produit.getNom());
        typeLabel.setText("animale".equalsIgnoreCase(produit.getType()) ? "Animaux" : "Vegetaux");
        prixLabel.setText(String.format("%.2f DT", produit.getPrix()));
        uniteLabel.setText(produit.getUnite());
        stockLabel.setText(String.valueOf(produit.getStock()));
        badgeLabel.setText(produit.getBadge() == null || produit.getBadge().isBlank() ? "-" : produit.getBadge());
        bioLabel.setText(produit.isBio() ? "Oui" : "Non");
        descriptionLabel.setText(produit.getDescription() == null ? "" : produit.getDescription());
        productImageView.setImage(resolveImage(produit.getImageUrl()));
        
        // Disable add to cart if out of stock
        addToCartBtn.setDisable(produit.getStock() == 0);
        addToCartBtn.setText(produit.getStock() == 0 ? "Épuisé" : "Ajouter au panier");
    }

    @FXML
    private void handleAddToCart() {
        if (addToCartAction != null) {
            addToCartAction.run();
        }
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) rootBox.getScene().getWindow();
        stage.close();
    }

    private Image resolveImage(String imageUrl) {
        try {
            if (imageUrl != null && imageUrl.startsWith("/")) {
                return new Image(getClass().getResource(imageUrl).toExternalForm(), true);
            }
            if (imageUrl != null && !imageUrl.isBlank()) {
                return new Image(imageUrl, true);
            }
        } catch (Exception ignored) {
        }
        return new Image(getClass().getResource("/esprit/tn/images/logo1.png").toExternalForm(), true);
    }
}
