package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import java.io.IOException;

public class AdminLayoutController {

    @FXML
    private StackPane contentArea;

    private static AdminLayoutController instance;

    public static AdminLayoutController getInstance() {
        return instance;
    }

    @FXML
    public void initialize() {
        instance = this;
        // Load Dashboard by default
        loadView("/esprit/tn/fxml/admin_dashboard.fxml");
    }

    @FXML
    private void showDashboard(ActionEvent event) {
        loadView("/esprit/tn/fxml/admin_dashboard.fxml");
    }

    @FXML
    private void showUsers(ActionEvent event) {
        loadView("/esprit/tn/fxml/admin_users.fxml");
    }

    @FXML
    private void showLieux(ActionEvent event) {
        loadView("/esprit/tn/fxml/admin_lieux.fxml");
    }

    @FXML
    private void showEvenements(ActionEvent event) {
        loadView("/esprit/tn/fxml/admin_evenements.fxml");
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        utils.NavigationService.navigateFromNode(contentArea, "/esprit/tn/fxml/home.fxml", "Bienvenue");
    }

    @FXML
    private void goToSite(ActionEvent event) {
        utils.NavigationService.navigateFromNode(contentArea, "/esprit/tn/fxml/front.fxml", "Front Office");
    }

    public void loadView(String fxmlPath) {
        try {
            Parent view = FXMLLoader.load(getClass().getResource(fxmlPath));
            contentArea.getChildren().setAll(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public <T> T loadViewWithController(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent view = loader.load();
            contentArea.getChildren().setAll(view);
            return loader.getController();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
