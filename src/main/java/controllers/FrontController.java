package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import services.ChatbotService;
import utils.UserSession;
import utils.NavigationService;

public class FrontController {

    @FXML private Label welcomeLabel;

    @FXML private HBox chatHeader;
    @FXML private AnchorPane chatbotContainer;
    @FXML private Button chatbotToggleButton;
    @FXML private VBox chatMessages;
    @FXML private ScrollPane chatScrollPane;
    @FXML private TextField chatInput;

    private final ChatbotService chatbotService = new ChatbotService();
    private double chatOffsetX, chatOffsetY;
    private double buttonOffsetX, buttonOffsetY;

    @FXML
    public void initialize() {
        String userName = UserSession.getInstance().getUserName();
        welcomeLabel.setText("Bonjour, " + (userName != null ? userName : "Utilisateur"));

        // ================= CHATBOT INITIALIZATION =================
        boolean isAdmin = false;
        try {
            isAdmin = UserSession.getInstance().isAdmin();
        } catch (Exception ignored) {}

        boolean showChatbot = !isAdmin;
        chatbotToggleButton.setVisible(showChatbot);
        chatbotToggleButton.setManaged(showChatbot);
        chatbotContainer.setVisible(false);
        chatbotContainer.setManaged(false);

        if (showChatbot) {
            addBubble("Bienvenue ! Je suis ton assistant Firmetna. Comment puis-je t'aider ?", false);
        }

        // ================= DRAG LOGIC =================
        setupDragLogic();
    }

    private void setupDragLogic() {
        chatHeader.setOnMousePressed(e -> {
            chatOffsetX = e.getSceneX();
            chatOffsetY = e.getSceneY();
        });

        chatHeader.setOnMouseDragged(e -> {
            double deltaX = e.getSceneX() - chatOffsetX;
            double deltaY = e.getSceneY() - chatOffsetY;
            chatbotContainer.setLayoutX(chatbotContainer.getLayoutX() + deltaX);
            chatbotContainer.setLayoutY(chatbotContainer.getLayoutY() + deltaY);
            chatOffsetX = e.getSceneX();
            chatOffsetY = e.getSceneY();
        });

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
            chatbotContainer.setVisible(false);
            chatbotContainer.setManaged(false);
        }
    }

    @FXML
    private void handleSendChat() {
        String question = chatInput.getText();
        if (question == null || question.trim().isEmpty()) return;

        addBubble(question, true);
        chatInput.clear();

        Label typing = new Label("L'assistant réfléchit...");
        typing.setStyle("-fx-text-fill: #888; -fx-font-style: italic;");
        chatMessages.getChildren().add(typing);

        new Thread(() -> {
            try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
            String answer = chatbotService.ask(question);
            javafx.application.Platform.runLater(() -> {
                chatMessages.getChildren().remove(typing);
                addBubble(answer, false);
                chatScrollPane.setVvalue(1.0);
            });
        }).start();
    }

    private void addBubble(String text, boolean isUser) {
        Label label = new Label(text);
        label.setWrapText(true);
        label.setMaxWidth(220);
        label.setPadding(new Insets(8, 12, 8, 12));

        if (isUser) {
            label.setStyle("-fx-background-color: #e3f2fd; -fx-background-radius: 15 15 0 15; -fx-text-fill: #1565c0;");
        } else {
            label.setStyle("-fx-background-color: #f1f8e9; -fx-background-radius: 15 15 15 0; -fx-text-fill: #2e7d32;");
        }

        HBox container = new HBox(label);
        container.setAlignment(isUser ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        chatMessages.getChildren().add(container);
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

    @FXML private void handleEvenements(ActionEvent event) { System.out.println("Opening Evenements"); }
    @FXML
    private void handleForum(ActionEvent event) {
        NavigationService.switchScene(event, "/esprit/tn/fxml/forum.fxml", "Forum - Firmetna");
    }
    @FXML private void handleNotifications(ActionEvent event) { System.out.println("Opening Notifications"); }
    @FXML private void handleDons(ActionEvent event) { System.out.println("Opening Dons"); }
    @FXML private void handlePartenariats(ActionEvent event) { System.out.println("Opening Partenariats"); }
}
