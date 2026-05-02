package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
<<<<<<< HEAD
import javafx.scene.control.Label;
import utils.UserSession;
import utils.NavigationService;
=======
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import services.ChatbotService;
import utils.UserSession;
import utils.NavigationService;
import services.ProduitService;
import services.EvenementService;
import services.PartnerService;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.io.IOException;
import models.User;
import services.UserService;
>>>>>>> gestion-user

public class FrontController {

    @FXML private Label welcomeLabel;
<<<<<<< HEAD

    @FXML
    public void initialize() {
        String userName = UserSession.getInstance().getUserName();
        welcomeLabel.setText("Bonjour, " + (userName != null ? userName : "Utilisateur"));
=======
    @FXML private Label lblTotalProducts;
    @FXML private Label lblTotalEvents;
    @FXML private Label lblTotalPartners;
    
    // Plant Gamification
    @FXML private ImageView imgPlant;
    @FXML private Label lblPlantLevel;
    @FXML private Label lblPlantStatus;
    @FXML private Label lblPlantHint;
    @FXML private ProgressBar progressPlant;

    private ProduitService produitService;
    private EvenementService evenementService;
    private PartnerService partnerService;
    private UserService userService;

    @FXML
    public void initialize() {
        produitService = new ProduitService();
        evenementService = new EvenementService();
        partnerService = new PartnerService();
        userService = new UserService();

        // Refresh user data from DB to get latest actions_count
        User current = UserSession.getInstance().getUser();
        if (current != null) {
            User updated = userService.getUserById(current.getId());
            if (updated != null) {
                UserSession.getInstance().setUser(updated);
            }
        }

        String userName = UserSession.getInstance().getUserName();
        welcomeLabel.setText("Bonjour, " + (userName != null ? userName : "Utilisateur"));

        loadStatistics();
        updatePlantUI();
    }

    private void updatePlantUI() {
        User currentUser = UserSession.getInstance().getUser();
        if (currentUser == null) return;

        int actions = currentUser.getActionsCount();
        String levelName;
        String imagePath;
        double progress;

        if (actions <= 2) {
            levelName = "Stade : Graine";
            imagePath = "/esprit/tn/images/plant/seed.png";
            progress = actions / 3.0; // 0 to 0.66
        } else if (actions <= 5) {
            levelName = "Stade : Pousse";
            imagePath = "/esprit/tn/images/plant/sprout.png";
            progress = (actions - 2) / 4.0 + 0.25; 
        } else if (actions <= 9) {
            levelName = "Stade : Fleur";
            imagePath = "/esprit/tn/images/plant/flower.png";
            progress = (actions - 5) / 5.0 + 0.5;
        } else {
            levelName = "Stade : Arbre";
            imagePath = "/esprit/tn/images/plant/tree.png";
            progress = 1.0;
        }

        lblPlantLevel.setText(levelName);
        progressPlant.setProgress(progress);

        try {
            var resource = getClass().getResource(imagePath);
            if (resource != null) {
                imgPlant.setImage(new Image(resource.toExternalForm()));
                imgPlant.setVisible(true);
            } else {
                // Fallback: Use Emoji in a tooltip or just a print for now
                System.out.println("Image missing: " + imagePath);
                // We could overlay an emoji label here if we had one in FXML
            }
        } catch (Exception e) {
            System.err.println("Could not load plant image: " + imagePath);
        }
    }

    private void loadStatistics() {
        try {
            int productsCount = produitService.countAll();
            int eventsCount = evenementService.getAll().size();
            int partnersCount = partnerService.getAll().size();

            lblTotalProducts.setText(productsCount + (productsCount >= 30 ? "+" : ""));
            lblTotalEvents.setText(String.valueOf(eventsCount));
            lblTotalPartners.setText(String.valueOf(partnersCount));
        } catch (Exception e) {
            System.err.println("Error loading front statistics: " + e.getMessage());
        }
>>>>>>> gestion-user
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        UserSession.getInstance().cleanUserSession();
        NavigationService.switchScene(event, "/esprit/tn/fxml/home.fxml", "Bienvenue");
    }

    @FXML
    private void handleProfil(ActionEvent event) {
        NavigationService.switchScene(event, "/esprit/tn/fxml/profile.fxml", "Mon Profil");
    }

    @FXML
    private void handleAccueil(ActionEvent event) {
        NavigationService.switchScene(event, "/esprit/tn/fxml/front.fxml", "Accueil");
    }
    
<<<<<<< HEAD
    @FXML private void handleProduits(ActionEvent event) { System.out.println("Opening Produits"); }
    @FXML private void handleEvenements(ActionEvent event) {
        NavigationService.switchScene(event, "/esprit/tn/fxml/front_evenements.fxml", "Événements");
    }
    @FXML private void handleForum(ActionEvent event) { System.out.println("Opening Forum"); }
    @FXML private void handleNotifications(ActionEvent event) { System.out.println("Opening Notifications"); }
    @FXML private void handleDons(ActionEvent event) { 
        NavigationService.switchScene(event, "/esprit/tn/fxml/admin_donations_offres.fxml", "Dons");
    }
    @FXML private void handlePartenariats(ActionEvent event) { System.out.println("Opening Partenariats"); }
=======
    @FXML 
    private void handleProduits(ActionEvent event) { 
        NavigationService.switchScene(event, "/esprit/tn/fxml/product_marketplace.fxml", "Marketplace");
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
    private void handlePanier(ActionEvent event) {
        System.out.println("Opening Panier from Front");
    }

    @FXML 
    private void handleEvenements(ActionEvent event) { 
        NavigationService.switchScene(event, "/esprit/tn/fxml/front_evenements.fxml", "Événements");
    }

    @FXML
    private void handleForum(ActionEvent event) {
        NavigationService.switchScene(event, "/esprit/tn/fxml/forum.fxml", "Forum - Firmetna");
    }

    @FXML private void handleNotifications(ActionEvent event) { System.out.println("Opening Notifications"); }

    @FXML 
    private void handleDons(ActionEvent event) { 
        NavigationService.switchScene(event, "/esprit/tn/fxml/front_donations_offres.fxml", "Donations & Solidarité");
    }
    @FXML 
    private void handlePartenariats(ActionEvent event) { 
        NavigationService.switchScene(event, "/esprit/tn/fxml/FrontPartnerView.fxml", "Espace Partenaires");
    }
>>>>>>> gestion-user

    @FXML
    private void handleOpenChatbot(ActionEvent event) {
        try {
<<<<<<< HEAD
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/esprit/tn/fxml/chatbot.fxml"));
            javafx.scene.Parent root = loader.load();
            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle("Assistant Intelligent");
            stage.setScene(new javafx.scene.Scene(root));
            stage.initModality(javafx.stage.Modality.NONE); // Non-modal so they can use the app while chatting
            stage.show();
        } catch (java.io.IOException e) {
            e.printStackTrace();
            System.err.println("Impossible de charger le Chatbot : " + e.getMessage());
=======
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/esprit/tn/fxml/chatbot.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Chatbot Firmetna");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
>>>>>>> gestion-user
        }
    }
}
