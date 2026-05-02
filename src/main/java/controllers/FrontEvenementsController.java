package controllers;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import models.Evenement;
import services.EvenementService;
import models.User;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import utils.AlertUtils;
import utils.NavigationService;
import utils.UserSession;

public class FrontEvenementsController {

    @FXML private FlowPane eventContainer;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> sortCombo;

    private final EvenementService evenementService = new EvenementService();
    private List<Evenement> allEvents = new ArrayList<>();

    @FXML
    public void initialize() {
        sortCombo.setItems(FXCollections.observableArrayList("Plus récents", "Plus anciens"));
        sortCombo.setValue("Plus récents");

        loadEvents();

        searchField.textProperty().addListener((obs, oldV, newV) -> filterAndSort());
        sortCombo.valueProperty().addListener((obs, oldV, newV) -> filterAndSort());
    }

    private void loadEvents() {
        allEvents = evenementService.getAll();
        filterAndSort();
    }

    private void filterAndSort() {
        String search = searchField.getText().toLowerCase();
        List<Evenement> filtered = allEvents.stream()
                .filter(e -> e.getNom().toLowerCase().contains(search) || e.getDescription().toLowerCase().contains(search))
                .collect(Collectors.toList());

        String sort = sortCombo.getValue();
        if ("Plus récents".equals(sort)) {
            filtered.sort(Comparator.comparing(Evenement::getDateEvenement).reversed());
        } else if ("Plus anciens".equals(sort)) {
            filtered.sort(Comparator.comparing(Evenement::getDateEvenement));
        }

        renderEvents(filtered);
    }

    private void renderEvents(List<Evenement> events) {
        eventContainer.getChildren().clear();
        for (Evenement event : events) {
            eventContainer.getChildren().add(createEventCard(event));
        }
    }

    private VBox createEventCard(Evenement event) {
        VBox card = new VBox(15);
        card.getStyleClass().add("glass-card");
        card.setPrefWidth(280);
        card.setStyle("-fx-padding: 20; -fx-background-radius: 20;");

        ImageView imageView = new ImageView();
        try {
            if (event.getImage() != null && !event.getImage().isEmpty()) {
                imageView.setImage(new Image(event.getImage(), true));
            } else {
                imageView.setImage(new Image(getClass().getResource("/esprit/tn/images/logo1.png").toExternalForm()));
            }
        } catch (Exception e) {
            imageView.setImage(new Image(getClass().getResource("/esprit/tn/images/logo1.png").toExternalForm()));
        }
        imageView.setFitWidth(240);
        imageView.setFitHeight(150);
        imageView.setPreserveRatio(true);
        imageView.getStyleClass().add("event-card-image");

        Label title = new Label(event.getNom());
        title.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: #1b4332;");
        title.setWrapText(true);

        Label date = new Label("📅 " + event.getDateEvenement().toString());
        date.setStyle("-fx-text-fill: #2d5a27; -fx-font-weight: bold;");

        Label location = new Label("📍 " + (event.getLieuVille() != null ? event.getLieuVille() : "Lieu non précisé"));
        location.setStyle("-fx-text-fill: #666; -fx-font-size: 13;");

        Button detailsBtn = new Button("Voir Détails");
        detailsBtn.setStyle("-fx-background-color: #2d5a27; -fx-text-fill: white; -fx-background-radius: 10; -fx-padding: 8 20; -fx-cursor: hand;");
        detailsBtn.setMaxWidth(Double.MAX_VALUE);
        detailsBtn.setOnAction(e -> showEventDetails(event));

        card.getChildren().addAll(imageView, title, date, location, detailsBtn);
        
        // Hover effect
        card.setOnMouseEntered(e -> card.setStyle("-fx-padding: 20; -fx-background-radius: 20; -fx-background-color: rgba(255,255,255,0.9); -fx-scale-x: 1.02; -fx-scale-y: 1.02;"));
        card.setOnMouseExited(e -> card.setStyle("-fx-padding: 20; -fx-background-radius: 20; -fx-background-color: rgba(255,255,255,0.75); -fx-scale-x: 1; -fx-scale-y: 1;"));

        return card;
    }

    private void showEventDetails(Evenement event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/esprit/tn/fxml/evenement_details.fxml"));
            Parent root = loader.load();
            
            EvenementDetailsController controller = loader.getController();
            controller.setEvent(event);
            
            Stage stage = new Stage();
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.initOwner(eventContainer.getScene().getWindow());
            stage.setTitle("Détails de l'événement - " + event.getNom());
            stage.setScene(new javafx.scene.Scene(root));
            stage.showAndWait();
            
        } catch (IOException e) {
            e.printStackTrace();
            AlertUtils.showError("Erreur", "Impossible d'ouvrir les détails de l'événement.");
        }
    }


    @FXML
    private void handleAccueil(ActionEvent event) {
        NavigationService.switchScene(event, "/esprit/tn/fxml/front.fxml", "Accueil");
    }

    @FXML
    private void handleProfil(ActionEvent event) {
        if (UserSession.getInstance().getUser() == null) {
            NavigationService.switchScene(event, "/esprit/tn/fxml/home.fxml", "Bienvenue");
            return;
        }
        NavigationService.switchScene(event, "/esprit/tn/fxml/profile.fxml", "Mon Profil");
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        UserSession.getInstance().cleanUserSession();
        NavigationService.switchScene(event, "/esprit/tn/fxml/home.fxml", "Bienvenue");
    }

    @FXML
    private void handleProduits(ActionEvent event) {
        NavigationService.switchScene(event, "/esprit/tn/fxml/product_marketplace.fxml", "Produits");
    }

    @FXML
    private void handleProduitsVegetaux(ActionEvent event) {
        utils.ProductNavigationState.setSelectedType("vegetale");
        NavigationService.switchScene(event, "/esprit/tn/fxml/product_marketplace.fxml", "Marketplace - Vegetaux");
    }

    @FXML
    private void handleProduitsAnimaux(ActionEvent event) {
        utils.ProductNavigationState.setSelectedType("animale");
        NavigationService.switchScene(event, "/esprit/tn/fxml/product_marketplace.fxml", "Marketplace - Animaux");
    }

    @FXML
    private void handleForum(ActionEvent event) {
        NavigationService.switchScene(event, "/esprit/tn/fxml/forum.fxml", "Forum");
    }

    @FXML
    private void handleDons(ActionEvent event) {
        NavigationService.switchScene(event, "/esprit/tn/fxml/front_donations_offres.fxml", "Donations & Solidarité");
    }

    @FXML
    private void handlePartenariats(ActionEvent event) {
        NavigationService.switchScene(event, "/esprit/tn/fxml/FrontPartnerView.fxml", "Espace Partenaires");
    }

    @FXML
    private void handleOpenChatbot(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/esprit/tn/fxml/chatbot.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Chatbot Firmetna");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
