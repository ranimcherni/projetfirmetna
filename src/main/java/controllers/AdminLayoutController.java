package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
<<<<<<< HEAD
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
=======
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
>>>>>>> gestion-user
import java.io.IOException;

public class AdminLayoutController {

<<<<<<< HEAD
    @FXML
    private StackPane contentArea;
=======
    // ================= FXML ELEMENTS =================
    @FXML private AnchorPane chatbotContainer;
    @FXML private Button chatbotToggleButton;
    @FXML private VBox chatMessages;
    @FXML private ScrollPane chatScrollPane;
    @FXML private TextField chatInput;
    @FXML private StackPane contentArea;

    private final services.ChatbotService chatbotService = new services.ChatbotService();
    private double chatOffsetX, chatOffsetY;
    private double buttonOffsetX, buttonOffsetY;
>>>>>>> gestion-user

    private static AdminLayoutController instance;

    public static AdminLayoutController getInstance() {
        return instance;
    }

    @FXML
    public void initialize() {
<<<<<<< HEAD
        instance = this;
        // Load Dashboard by default
        loadView("/esprit/tn/fxml/admin_dashboard.fxml");
    }

=======
        // ================= INSTANCE =================
        instance = this;

        // ================= SECURITY =================
        try {
            if (!utils.UserSession.getInstance().isAdmin()) {
                handleLogout(null);
                return;
            }
        } catch (Exception e) { e.printStackTrace(); }

        // ================= CHATBOT =================
        chatbotContainer.setVisible(false);
        chatbotContainer.setManaged(false);

        setupDragLogic();

        // ================= DEFAULT PAGE =================
        loadView("/esprit/tn/fxml/admin_dashboard.fxml");
    }

    private void setupDragLogic() {
        chatbotToggleButton.setOnMousePressed(e -> {
            buttonOffsetX = e.getSceneX() - chatbotToggleButton.getLayoutX();
            buttonOffsetY = e.getSceneY() - chatbotToggleButton.getLayoutY();
        });
        chatbotToggleButton.setOnMouseDragged(e -> {
            chatbotToggleButton.setLayoutX(e.getSceneX() - buttonOffsetX);
            chatbotToggleButton.setLayoutY(e.getSceneY() - buttonOffsetY);
        });

        chatbotContainer.setOnMousePressed(e -> {
            chatOffsetX = e.getSceneX() - chatbotContainer.getLayoutX();
            chatOffsetY = e.getSceneY() - chatbotContainer.getLayoutY();
        });
        chatbotContainer.setOnMouseDragged(e -> {
            chatbotContainer.setLayoutX(e.getSceneX() - chatOffsetX);
            chatbotContainer.setLayoutY(e.getSceneY() - chatOffsetY);
        });
    }

    @FXML
    private void toggleChatbot() {
        chatbotContainer.setVisible(!chatbotContainer.isVisible());
        chatbotContainer.setManaged(chatbotContainer.isVisible());
    }

    @FXML
    private void handleSendChat() {
        String text = chatInput.getText();
        if (text == null || text.trim().isEmpty()) return;

        addBubble(text, true);
        chatInput.clear();

        String response = chatbotService.sendMessage(text);
        addBubble(response, false);
        chatScrollPane.setVvalue(1.0);
    }

    private void addBubble(String text, boolean isUser) {
        Label label = new Label((isUser ? "Vous: " : "Bot: ") + text);
        label.setWrapText(true);
        label.setMaxWidth(220);
        chatMessages.getChildren().add(label);
    }

>>>>>>> gestion-user
    @FXML
    private void showDashboard(ActionEvent event) {
        loadView("/esprit/tn/fxml/admin_dashboard.fxml");
    }

    @FXML
    private void showUsers(ActionEvent event) {
        loadView("/esprit/tn/fxml/admin_users.fxml");
    }

    @FXML
<<<<<<< HEAD
=======
    private void showProducts(ActionEvent event) {
        loadView("/esprit/tn/fxml/admin_products.fxml");
    }

    @FXML
    private void showCommandes(ActionEvent event) {
        loadView("/esprit/tn/fxml/admin_commandes.fxml");
    }

    @FXML
    private void showForum(ActionEvent event) {
        loadView("/esprit/tn/fxml/forum.fxml");
    }

    @FXML
    private void showForumPublications(ActionEvent event) {
        loadView("/esprit/tn/fxml/admin_forum_publications.fxml");
    }

    @FXML
    private void showForumComments(ActionEvent event) {
        loadView("/esprit/tn/fxml/admin_forum_comments.fxml");
    }

    @FXML
    private void showForumStats(ActionEvent event) {
        loadView("/esprit/tn/fxml/admin_forum_stats.fxml");
    }

    @FXML
    private void showEvents(ActionEvent event) {
        loadView("/esprit/tn/fxml/admin_evenements.fxml");
    }

    @FXML
>>>>>>> gestion-user
    private void showLieux(ActionEvent event) {
        loadView("/esprit/tn/fxml/admin_lieux.fxml");
    }

    @FXML
<<<<<<< HEAD
    private void showEvenements(ActionEvent event) {
        loadView("/esprit/tn/fxml/admin_evenements.fxml");
    }

    @FXML
    private void showDonationOffres(ActionEvent event) {
=======
    private void showDonationsOffres(ActionEvent event) {
>>>>>>> gestion-user
        loadView("/esprit/tn/fxml/admin_donations_offres.fxml");
    }

    @FXML
<<<<<<< HEAD
    private void showDonationDemandes(ActionEvent event) {
=======
    private void showDonationsDemandes(ActionEvent event) {
>>>>>>> gestion-user
        loadView("/esprit/tn/fxml/admin_donations_demandes.fxml");
    }

    @FXML
<<<<<<< HEAD
    private void showDonationCalendar(ActionEvent event) {
        loadView("/esprit/tn/fxml/admin_donations_calendar.fxml");
=======
    private void showPartenariats(ActionEvent event) {
        loadView("/esprit/tn/fxml/PartnerView.fxml");
    }

    @FXML
    private void showOffresPartenaires(ActionEvent event) {
        loadView("/esprit/tn/fxml/ContractView.fxml");
>>>>>>> gestion-user
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
