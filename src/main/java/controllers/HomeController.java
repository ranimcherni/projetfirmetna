package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import utils.UserSession;



public class HomeController {
    
    @FXML private Label welcomeLabel;
    @FXML private Button loginBtn;
    @FXML private Button logoutBtn;
    @FXML private VBox profileBox;

    @FXML
    public void initialize() {
        String name = UserSession.getInstance().getUserName();
        if (name != null) {
            welcomeLabel.setText("Bonjour " + name);
            logoutBtn.setVisible(true);
            logoutBtn.setManaged(true);
            loginBtn.setVisible(false);
            loginBtn.setManaged(false);
            profileBox.setVisible(true);
            profileBox.setManaged(true);
        } else {
            welcomeLabel.setText("Bonjour !");
            logoutBtn.setVisible(false);
            logoutBtn.setManaged(false);
            loginBtn.setVisible(true);
            loginBtn.setManaged(true);
            profileBox.setVisible(false);
            profileBox.setManaged(false);
        }
    }

    @FXML
    public void handleGoToLogin(ActionEvent event) {
        utils.NavigationService.switchScene(event, "/esprit/tn/fxml/login.fxml", "Connexion");
    }

    @FXML
    public void handleLogout(ActionEvent event) {
        UserSession.getInstance().cleanUserSession();
        // Simply refresh the current page to reflect the logged-out state
        initialize();
    }
}

