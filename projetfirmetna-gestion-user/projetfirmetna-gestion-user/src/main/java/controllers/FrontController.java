package controllers;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import controllers.AdminLayoutController;
import services.ChatbotService;
import utils.UserSession;

public class FrontController {
    @FXML private HBox chatHeader;

    @FXML private AnchorPane chatbotContainer;
    @FXML private Button chatbotToggleButton;
    @FXML private VBox chatMessages;
    @FXML private ScrollPane chatScrollPane;
    @FXML private TextField chatInput;
    @FXML private MenuItem backToAdminMenuItem;
    private final ChatbotService chatbotService = new ChatbotService();
    private double chatOffsetX, chatOffsetY;
    private double buttonOffsetX, buttonOffsetY;
    @FXML
    public void initialize() {

        boolean isAdmin = false;

        try {
            isAdmin = UserSession.getInstance().isAdmin();
        } catch (Exception ignored) {}

        boolean showChatbot = !isAdmin;

        // ================= CHATBOT VISIBILITY =================
        chatbotToggleButton.setVisible(showChatbot);
        chatbotToggleButton.setManaged(showChatbot);

        chatbotContainer.setVisible(false);
        chatbotContainer.setManaged(false);

        // ================= ADMIN MENU =================
        if (backToAdminMenuItem != null) {
            backToAdminMenuItem.setVisible(isAdmin);
        }

        // ================= WELCOME MESSAGE =================
        if (showChatbot) {
            addBubble("Bienvenue ! Je suis ton assistant projet.", false);
        }

        // ================= DRAG CHAT =================
        chatHeader.setOnMousePressed(e -> {
            chatOffsetX = e.getSceneX();
            chatOffsetY = e.getSceneY();

            chatHeader.getScene().setOnMouseDragged(event -> {

                double deltaX = event.getSceneX() - chatOffsetX;
                double deltaY = event.getSceneY() - chatOffsetY;

                chatbotContainer.setLayoutX(chatbotContainer.getLayoutX() + deltaX);
                chatbotContainer.setLayoutY(chatbotContainer.getLayoutY() + deltaY);

                chatOffsetX = event.getSceneX();
                chatOffsetY = event.getSceneY();
            });
        });

        // ================= RELEASE + SNAP =================
        chatHeader.setOnMouseReleased(e -> {

            // stop dragging
            chatHeader.getScene().setOnMouseDragged(null);

            double sceneWidth = chatbotContainer.getScene().getWidth();
            double middle = sceneWidth / 2;

            double targetX;

            if (chatbotContainer.getLayoutX() > middle) {
                targetX = sceneWidth - chatbotContainer.getWidth() - 20;
            } else {
                targetX = 20;
            }

            javafx.animation.KeyValue kv =
                    new javafx.animation.KeyValue(chatbotContainer.layoutXProperty(), targetX);

            javafx.animation.KeyFrame kf =
                    new javafx.animation.KeyFrame(javafx.util.Duration.millis(200), kv);

            new javafx.animation.Timeline(kf).play();
        });

        // ================= DRAG BUTTON =================
        chatbotToggleButton.setOnMousePressed(e -> {
            buttonOffsetX = e.getSceneX();
            buttonOffsetY = e.getSceneY();
        });

        chatbotToggleButton.setOnMouseDragged(e -> {

            double deltaX = e.getSceneX() - buttonOffsetX;
            double deltaY = e.getSceneY() - buttonOffsetY;

            chatbotToggleButton.setLayoutX(chatbotToggleButton.getLayoutX() + deltaX);
            chatbotToggleButton.setLayoutY(chatbotToggleButton.getLayoutY() + deltaY);

            buttonOffsetX = e.getSceneX();
            buttonOffsetY = e.getSceneY();
        });
    }
    @FXML
    private void toggleChatbot() {

        boolean isVisible = chatbotContainer.isVisible();

        if (!isVisible) {
            chatbotContainer.setVisible(true);
            chatbotContainer.setManaged(true);

            chatbotContainer.setOpacity(0);
            chatbotContainer.setTranslateY(20);

            javafx.animation.FadeTransition fade = new javafx.animation.FadeTransition(javafx.util.Duration.millis(200), chatbotContainer);
            fade.setToValue(1);

            javafx.animation.TranslateTransition slide = new javafx.animation.TranslateTransition(javafx.util.Duration.millis(200), chatbotContainer);
            slide.setToY(0);

            fade.play();
            slide.play();

        } else {

            javafx.animation.FadeTransition fade = new javafx.animation.FadeTransition(javafx.util.Duration.millis(200), chatbotContainer);
            fade.setToValue(0);

            fade.setOnFinished(e -> {
                chatbotContainer.setVisible(false);
                chatbotContainer.setManaged(false);
            });

            fade.play();
        }
    }
    @FXML
    private void handleSendChat() {
        String question = chatInput.getText().trim();
        if (question.isEmpty()) return;

        // USER MESSAGE
        addBubble(question, true);
        chatInput.clear();

        // BOT TYPING...
        Label typing = new Label("Typing...");
        chatMessages.getChildren().add(typing);
        chatScrollPane.setVvalue(1.0);

        new Thread(() -> {
            try {
                Thread.sleep(1000); // simulate thinking
            } catch (Exception ignored) {}

            String answer = chatbotService.ask(question);

            javafx.application.Platform.runLater(() -> {
                chatMessages.getChildren().remove(typing);
                addBubble(answer, false);
                chatScrollPane.setVvalue(1.0);
            });
        }).start();
    }
    private void addBubble(String text, boolean isUser) {
        Label bubble = new Label(text);
        bubble.setWrapText(true);
        bubble.setMaxWidth(260);
        bubble.setStyle(isUser
                ? "-fx-background-color: #2e8b57; -fx-text-fill: white; -fx-padding: 10 14; -fx-background-radius: 18 18 2 18;"
                : "-fx-background-color: #f4f6f8; -fx-text-fill: #212121; -fx-padding: 10 14; -fx-background-radius: 18 18 18 2;");

        HBox wrapper = new HBox(bubble);
        wrapper.setAlignment(isUser ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        wrapper.setStyle("-fx-padding: 4 0 4 0;");

        chatMessages.getChildren().add(wrapper);
        chatScrollPane.layout();
        chatScrollPane.setVvalue(1.0);
    }

    // ================= NAVIGATION =================

    private void navigate(String path) {
        try {
            // If the admin layout is active, delegate view loading to it so the chatbot stays visible
            AdminLayoutController admin = AdminLayoutController.getInstance();
            if (admin != null) {
                admin.loadView(path);
                return;
            }

            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource(path));
            javafx.scene.Parent root = loader.load();

            // Preserve chatbot when switching views by placing the new view and the chatbot in a StackPane
            javafx.stage.Stage stage = (javafx.stage.Stage) chatbotToggleButton.getScene().getWindow();
            javafx.scene.Scene scene = stage.getScene();

            // detach chatbotContainer from its current parent so it can be re-used
            if (chatbotContainer.getParent() instanceof javafx.scene.layout.Pane) {
                ((javafx.scene.layout.Pane) chatbotContainer.getParent()).getChildren().remove(chatbotContainer);
            }

            javafx.scene.layout.StackPane stack = new javafx.scene.layout.StackPane();
            stack.getChildren().add(root);
            stack.getChildren().add(chatbotContainer);

            scene.setRoot(stack);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAccueil() {
        navigate("/esprit/tn/fxml/front.fxml");
    }

    @FXML
    private void handleProduits() {
        navigate("/esprit/tn/fxml/product_marketplace.fxml");
    }

    @FXML
    private void handleProduitsVegetaux() {
        navigate("/esprit/tn/fxml/product_marketplace.fxml");
    }

    @FXML
    private void handleProduitsAnimaux() {
        navigate("/esprit/tn/fxml/product_marketplace.fxml");
    }

    @FXML
    private void handleEvenements() {
        navigate("/esprit/tn/fxml/home.fxml");
    }

    @FXML
    private void handleForum() {
        navigate("/esprit/tn/fxml/home.fxml");
    }

    @FXML
    private void handleNotifications() {
        navigate("/esprit/tn/fxml/home.fxml");
    }

    @FXML
    private void handleDons() {
        navigate("/esprit/tn/fxml/home.fxml");
    }

    @FXML
    private void handlePartenariats() {
        navigate("/esprit/tn/fxml/home.fxml");
    }

    @FXML
    private void handleProfil() {
        navigate("/esprit/tn/fxml/profile.fxml");
    }

    @FXML
    private void handleBackToAdmin() {
        navigate("/esprit/tn/fxml/admin_layout.fxml");
    }
    @FXML
    private void handleLogout() {
        navigate("/esprit/tn/fxml/login.fxml");
    }
}