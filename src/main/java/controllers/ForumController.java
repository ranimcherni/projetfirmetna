package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import models.Publication;
import services.ServiceCommentaire;
import services.ServicePublication;
import services.ServiceReaction;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class ForumController implements Initializable {

    @FXML
    private ListView<Publication> listPublications;
    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<String> comboFilter;

    private ServicePublication sp = new ServicePublication();
    private ServiceReaction sr = new ServiceReaction();
    private ServiceCommentaire sc = new ServiceCommentaire();
    private ObservableList<Publication> obsList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        comboFilter.getItems().addAll("Toutes", "Discussion", "Question", "Annonce");
        comboFilter.setValue("Toutes");

        loadData();

        listPublications.setCellFactory(param -> new ListCell<Publication>() {
            @Override
            protected void updateItem(Publication p, boolean empty) {
                super.updateItem(p, empty);
                if (empty || p == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    // Create modern card container
                    VBox card = new VBox(8);
                    card.getStyleClass().add("publication-card-item");
                    card.setPrefWidth(listPublications.getWidth() - 40);

                    // Header row: Title + Type Badge
                    HBox header = new HBox(15);
                    header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

                    Label title = new Label(p.getTitre());
                    title.getStyleClass().add("header-title-sm");
                    title.setStyle("-fx-font-size: 18px;"); // Keep size adjustment but use class for color/font
                    
                    Label typeBadge = new Label(p.getType().toUpperCase());
                    typeBadge.getStyleClass().add("badge-type");

                    Region spacer = new Region();
                    HBox.setHgrow(spacer, Priority.ALWAYS);

                    Label date = new Label(p.getDateCreation().toString().substring(0, 16));
                    date.getStyleClass().add("text-muted");

                    header.getChildren().addAll(title, typeBadge, spacer, date);

                    // Content Snippet
                    String snippet = p.getContenu();
                    if (snippet.length() > 120) snippet = snippet.substring(0, 117) + "...";
                    Label content = new Label(snippet);
                    content.setWrapText(true);
                    content.setStyle("-fx-text-fill: #444; -fx-font-size: 14px;");

                    // Main card body (HBox to include Image + Content)
                    HBox cardBody = new HBox(15);
                    cardBody.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

                    if (p.getImagePath() != null && !p.getImagePath().isEmpty()) {
                        try {
                            java.io.File file = new java.io.File(p.getImagePath());
                            if (file.exists()) {
                                Image img = new Image(file.toURI().toString());
                                ImageView thumb = new ImageView(img);
                                thumb.setFitWidth(100);
                                thumb.setFitHeight(100);
                                thumb.setPreserveRatio(true);
                                thumb.setSmooth(true);
                                
                                // Wrap in styled container for rounded corners
                                StackPane thumbContainer = new StackPane(thumb);
                                thumbContainer.setStyle("-fx-background-color: #f1f1f1; -fx-background-radius: 10;");
                                thumbContainer.setPrefSize(100, 100);
                                
                                cardBody.getChildren().add(thumbContainer);
                            }
                        } catch (Exception e) {
                            // Skip thumbnail on error
                        }
                    }

                    // Text Content VBox
                    VBox textContent = new VBox(8);
                    textContent.getChildren().addAll(header, content);
                    HBox.setHgrow(textContent, Priority.ALWAYS);

                    cardBody.getChildren().add(textContent);

                    card.getChildren().add(cardBody);

                    // Like count badge (read-only)
                    int likeCount = sr.countLikes("publication", p.getId());
                    int dislikeCount = sr.countDislikes("publication", p.getId());
                    HBox reactionRow = new HBox(8);
                    reactionRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                    Label likeBadge = new Label("👍 " + likeCount);
                    likeBadge.getStyleClass().add("reaction-count");
                    likeBadge.setStyle("-fx-text-fill: #2ecc71; -fx-font-size: 12px; -fx-font-weight: bold;");
                    Label dislikeBadge = new Label("👎 " + dislikeCount);
                    dislikeBadge.getStyleClass().add("reaction-count");
                    dislikeBadge.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 12px; -fx-font-weight: bold;");
                    reactionRow.getChildren().addAll(likeBadge, dislikeBadge);
                    card.getChildren().add(reactionRow);

                    // --- HOVER PREVIEW (TOOLTIP) ---
                    int commentCount = sc.getByPublication(p.getId()).size();
                    String previewText = p.getContenu();
                    if (previewText.length() > 500) previewText = previewText.substring(0, 497) + "...";
                    
                    Tooltip tooltip = new Tooltip(
                        "📄 APERÇU DU CONTENU :\n" + previewText + 
                        "\n\n💬 " + commentCount + " Commentaires"
                    );
                    tooltip.setWrapText(true);
                    tooltip.setPrefWidth(350);
                    tooltip.setShowDelay(javafx.util.Duration.millis(300));
                    tooltip.setStyle("-fx-font-size: 13px; -fx-background-color: #1b4332; -fx-text-fill: white; -fx-padding: 15; -fx-background-radius: 10;");
                    
                    Tooltip.install(card, tooltip);

                    setGraphic(card);
                    setText(null);
                }
            }
        });

        // Search logic
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterList(newValue, comboFilter.getValue());
        });

        comboFilter.valueProperty().addListener((observable, oldValue, newValue) -> {
            filterList(searchField.getText(), newValue);
        });

        listPublications.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                openSelectedPublication();
            }
        });
    }

    private void loadData() {
        try {
            obsList.clear();
            obsList.addAll(sp.getAll());
            listPublications.setItems(obsList);
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur de chargement");
            alert.setHeaderText("Impossible de charger les publications");
            alert.setContentText(e.getMessage());
            alert.show();
        }
    }

    private void filterList(String keyword, String type) {
        List<Publication> items = sp.search(keyword);
        obsList.clear();

        for (Publication p : items) {
            if ("Toutes".equals(type) || p.getType().equals(type)) {
                obsList.add(p);
            }
        }
        listPublications.setItems(obsList);
    }

    @FXML
    private void switchToAddPublication(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/esprit/tn/fxml/add_publication.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 900, 600));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void openSelectedPublication() {
        Publication selected = listPublications.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/esprit/tn/fxml/show_publication.fxml"));
            Parent root = loader.load();

            ShowPublicationController controller = loader.getController();
            controller.initData(selected);

            Stage stage = (Stage) listPublications.getScene().getWindow();
            stage.setScene(new Scene(root, 900, 600));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void deletePublicationAction(ActionEvent event) {
        Publication selected = listPublications.getSelectionModel().getSelectedItem();
        if (selected != null) {
            sp.delete(selected);
            loadData(); // refresh list
        }
    }

    @FXML
    private void returnToHome(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/esprit/tn/fxml/home.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            // might be missing home.fxml currently, print
            System.err.println("home.fxml not found. Returning to Admin/Front?");
        }
    }
}
