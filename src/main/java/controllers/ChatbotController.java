package controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import services.ChatbotService;

public class ChatbotController {

    @FXML
    private ListView<ChatMessage> chatListView;
    @FXML
    private TextField inputField;

    private ChatbotService chatbotService;
    private ObservableList<ChatMessage> messages;

    @FXML
    public void initialize() {
        chatbotService = new ChatbotService();
        messages = FXCollections.observableArrayList();
        chatListView.setItems(messages);

        chatListView.setCellFactory(param -> new ListCell<ChatMessage>() {
            @Override
            protected void updateItem(ChatMessage item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                    setStyle("-fx-background-color: transparent;");
                } else {
                    Text text = new Text(item.content);
                    text.setWrappingWidth(250);
                    TextFlow textFlow = new TextFlow(text);

                    HBox hbox = new HBox(textFlow);
                    if (item.isUser) {
                        textFlow.setStyle("-fx-padding: 10; -fx-background-radius: 15; -fx-background-color: #2d5a27;");
                        text.setFill(javafx.scene.paint.Color.WHITE);
                        hbox.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
                    } else {
                        textFlow.setStyle("-fx-padding: 10; -fx-background-radius: 15; -fx-background-color: #e0e0e0;");
                        text.setFill(javafx.scene.paint.Color.BLACK);
                        hbox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                    }
                    setGraphic(hbox);
                    setStyle("-fx-background-color: transparent; -fx-padding: 5;");
                }
            }
        });

        messages.add(new ChatMessage("Bonjour ! Je suis l'assistant intelligent de Firmetna. Comment puis-je vous aider aujourd'hui ?", false));
    }

    @FXML
    private void handleSend() {
        String userInput = inputField.getText().trim();
        if (userInput.isEmpty()) return;

        messages.add(new ChatMessage(userInput, true));
        inputField.clear();
        chatListView.scrollTo(messages.size() - 1);

        inputField.setDisable(true);
        ChatMessage typingMsg = new ChatMessage("L'assistant réfléchit...", false);
        messages.add(typingMsg);
        chatListView.scrollTo(messages.size() - 1);

        new Thread(() -> {
            String response = chatbotService.sendMessage(userInput);
            Platform.runLater(() -> {
                messages.remove(typingMsg);
                messages.add(new ChatMessage(response, false));
                chatListView.scrollTo(messages.size() - 1);
                inputField.setDisable(false);
                inputField.requestFocus();
            });
        }).start();
    }

    private static class ChatMessage {
        String content;
        boolean isUser;

        ChatMessage(String content, boolean isUser) {
            this.content = content;
            this.isUser = isUser;
        }
    }
}
