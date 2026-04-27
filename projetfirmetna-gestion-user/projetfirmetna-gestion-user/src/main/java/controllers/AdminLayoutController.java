package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;

public class AdminLayoutController {

    // ================= STATIC INSTANCE =================
    private static AdminLayoutController instance;

    // ================= FXML ELEMENTS =================
    @FXML private AnchorPane chatbotContainer;
    @FXML private Button chatbotToggleButton;
    @FXML private VBox chatMessages;
    @FXML private ScrollPane chatScrollPane;
    @FXML private TextField chatInput;
    @FXML private AnchorPane contentArea;


    // ================= DRAG VARIABLES =================
    private double offsetX, offsetY;

    // ================= INITIALIZE =================
    @FXML
    public void initialize() {

        // ================= INSTANCE =================
        instance = this;

        // ================= SECURITY (ADMIN ONLY) =================
        try {
            if (!utils.UserSession.getInstance().isAdmin()) {
                handleLogout();
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // ================= DRAG BUTTON =================
        chatbotToggleButton.setOnMousePressed(e -> {
            offsetX = e.getSceneX() - chatbotToggleButton.getLayoutX();
            offsetY = e.getSceneY() - chatbotToggleButton.getLayoutY();
        });

        chatbotToggleButton.setOnMouseDragged(e -> {
            chatbotToggleButton.setLayoutX(e.getSceneX() - offsetX);
            chatbotToggleButton.setLayoutY(e.getSceneY() - offsetY);
        });

        // ================= DRAG CHAT WINDOW =================
        chatbotContainer.setOnMousePressed(e -> {
            offsetX = e.getSceneX() - chatbotContainer.getLayoutX();
            offsetY = e.getSceneY() - chatbotContainer.getLayoutY();
        });

        chatbotContainer.setOnMouseDragged(e -> {
            chatbotContainer.setLayoutX(e.getSceneX() - offsetX);
            chatbotContainer.setLayoutY(e.getSceneY() - offsetY);
        });

        // ================= DEFAULT PAGE =================
        loadView("/esprit/tn/fxml/admin_dashboard.fxml");
    }

    // ================= GET INSTANCE =================
    public static AdminLayoutController getInstance() {
        return instance;
    }

    // ================= LOAD VIEW =================


    // ================= CHATBOT =================
    @FXML
    private void toggleChatbot() {
        chatbotContainer.setVisible(!chatbotContainer.isVisible());
    }

    @FXML
    private void handleSendChat() {
        String text = chatInput.getText();
        if (text == null || text.isEmpty()) return;

        // USER MESSAGE
        Label userMsg = new Label("You: " + text);
        chatMessages.getChildren().add(userMsg);

        chatInput.clear();

        // BOT RESPONSE
        String response = new services.ChatbotService().ask(text);

        Label botMsg = new Label("Bot: " + response);
        chatMessages.getChildren().add(botMsg);

        // 🔥 ADD THIS LINE HERE (VERY IMPORTANT)
        chatScrollPane.setVvalue(1.0);
    }
    // ================= NAVIGATION =================
    @FXML
    private void showDashboard() {
        loadView("/esprit/tn/fxml/admin_dashboard.fxml");
    }

    @FXML
    private void showUsers() {
        loadView("/esprit/tn/fxml/admin_users.fxml");
    }

    @FXML
    private void showProducts() {
        loadView("/esprit/tn/fxml/admin_products.fxml");
    }

    @FXML
    private void showCommandes() {
        loadView("/esprit/tn/fxml/admin_commandes.fxml");
    }

    @FXML
    private void handleLogout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/esprit/tn/fxml/login.fxml"));
            Parent root = loader.load();

            chatbotContainer.getScene().setRoot(root);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void loadView(String path) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource(path));
            javafx.scene.Parent view = loader.load();

            contentArea.getChildren().setAll(view);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public <T> T loadViewWithController(String path) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource(path));
            javafx.scene.Parent view = loader.load();

            contentArea.getChildren().setAll(view);

            return loader.getController();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}