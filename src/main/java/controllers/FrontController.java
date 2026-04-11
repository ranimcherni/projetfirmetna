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
    @FXML private void handleEvenements(ActionEvent event) { System.out.println("Opening Evenements"); }
    @FXML private void handleForum(ActionEvent event) { System.out.println("Opening Forum"); }
    @FXML private void handleNotifications(ActionEvent event) { System.out.println("Opening Notifications"); }
    @FXML private void handleDons(ActionEvent event) { System.out.println("Opening Dons"); }
    @FXML private void handlePartenariats(ActionEvent event) { System.out.println("Opening Partenariats"); }
}
