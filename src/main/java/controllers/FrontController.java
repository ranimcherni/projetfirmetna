package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import utils.UserSession;
import utils.NavigationService;

public class FrontController {

    @FXML private Label welcomeLabel;

    @FXML
    public void initialize() {
        String userName = UserSession.getInstance().getUserName();
        welcomeLabel.setText("Bonjour, " + (userName != null ? userName : "Utilisateur"));
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

    @FXML
    private void handleOpenChatbot(ActionEvent event) {
        try {
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
        }
    }
}
