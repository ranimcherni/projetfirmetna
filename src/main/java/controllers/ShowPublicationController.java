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
import models.Commentaire;
import models.Publication;
import models.User;
import services.ServiceCommentaire;
import services.ServicePublication;
import services.ServiceReaction;
import services.HateSpeechService;
import services.UserService;
import utils.NavigationService;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class ShowPublicationController implements Initializable {

    @FXML
    private Label lblTitre;
    @FXML
    private Label lblType;
    @FXML
    private Label lblDate;
    @FXML
    private Label lblContenu;
    @FXML
    private ListView<Commentaire> listCommentaires;
    @FXML
    private TextArea txtNewComment;
    @FXML
    private TextField txtEditTitre;
    @FXML
    private TextArea txtEditContenu;
    @FXML
    private ImageView imgPublication;
    @FXML
    private StackPane imageContainer;
    @FXML
    private Button btnEdit;
    @FXML
    private Button btnSave;
    @FXML
    private Button btnCancel;
    @FXML
    private Button btnPostComment;
    @FXML
    private ComboBox<String> comboLanguage;
    @FXML
    private Button btnTranslateMain;
    @FXML
    private Button btnOpenPdf;
    @FXML
    private Button btnLikePublication;
    @FXML
    private Button btnDislikePublication;
    @FXML
    private Label lblLikesPublication;
    @FXML
    private Label lblDislikesPublication;

    private Publication currentPublication;
    private ServicePublication sp = new ServicePublication();
    private ServiceCommentaire sc = new ServiceCommentaire();
    private ServiceReaction sr = new ServiceReaction();
    private UserService us = new UserService();
    private ObservableList<Commentaire> obsList = FXCollections.observableArrayList();
    private Commentaire selectedCommentForEdit = null;
    private Integer selectedParentId = null;
    private String originalPublicationContent = "";
    private boolean isPublicationTranslated = false;
    // Active user ID — replace with session user when available
    private int activeUserId = 1;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        User currentUser = utils.UserSession.getInstance().getUser();
        if (currentUser != null) {
            activeUserId = currentUser.getId();
        }

        if(comboLanguage != null) {
            comboLanguage.getItems().addAll("Français", "Anglais", "Arabe", "Espagnol");
            comboLanguage.setValue("Français");
        }

        listCommentaires.setCellFactory(param -> new ListCell<Commentaire>() {
            @Override
            protected void updateItem(Commentaire c, boolean empty) {
                super.updateItem(c, empty);
                if (empty || c == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    VBox card = new VBox(5);
                    card.getStyleClass().add("comment-bubble");
                    card.setMinWidth(400);

                    HBox header = new HBox(10);
                    header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

                    User author = us.getUserById(c.getAuteurId());
                    String name = (author != null) ? (author.getPrenom() + " " + author.getNom()) : "Utilisateur Inconnu ("+c.getAuteurId()+")";
                    
                    Label lblAuthor = new Label(name);
                    lblAuthor.setStyle("-fx-font-weight: bold; -fx-text-fill: #1B4332; -fx-font-size: 13px;");

                    Region spacer = new Region();
                    HBox.setHgrow(spacer, Priority.ALWAYS);

                    Label lblDateC = new Label(c.getDateCreation() != null ? c.getDateCreation().toString().substring(0, 16) : "");
                    lblDateC.getStyleClass().add("text-muted");
                    lblDateC.setStyle("-fx-font-size: 10px;");

                    header.getChildren().addAll(lblAuthor, spacer, lblDateC);

                    Label content = new Label(c.getContenu());
                    content.setWrapText(true);
                    content.setStyle("-fx-text-fill: #333; -fx-font-size: 14px;");

                    card.getChildren().addAll(header, content);

                    // ── Reaction buttons for comment ──
                    HBox reactionBox = new HBox(8);
                    reactionBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

                    Button btnLikeC   = new Button("👍");
                    btnLikeC.getStyleClass().add("btn-like");
                    Label lblLikesC   = new Label(String.valueOf(sr.countLikes("commentaire", c.getId())));
                    lblLikesC.getStyleClass().add("reaction-count");

                    Button btnDislikeC = new Button("👎");
                    btnDislikeC.getStyleClass().add("btn-dislike");
                    Label lblDislikesC = new Label(String.valueOf(sr.countDislikes("commentaire", c.getId())));
                    lblDislikesC.getStyleClass().add("reaction-count");

                    // Highlight based on current user's reaction
                    String userReactionC = sr.getUserReaction(activeUserId, "commentaire", c.getId());
                    if ("like".equals(userReactionC))    btnLikeC.getStyleClass().add("btn-like-active");
                    if ("dislike".equals(userReactionC)) btnDislikeC.getStyleClass().add("btn-dislike-active");

                    btnLikeC.setOnAction(e -> {
                        sr.toggleReaction(activeUserId, "commentaire", c.getId(), "like");
                        loadComments(); // refresh cell
                    });
                    btnDislikeC.setOnAction(e -> {
                        sr.toggleReaction(activeUserId, "commentaire", c.getId(), "dislike");
                        loadComments();
                    });

                    reactionBox.getChildren().addAll(btnLikeC, lblLikesC, btnDislikeC, lblDislikesC);
                    card.getChildren().add(reactionBox);

                    HBox actions = new HBox(10);
                    actions.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);

                    Button btnReply = new Button("Répondre");
                    btnReply.setStyle("-fx-background-color: transparent; -fx-text-fill: #27ae60; -fx-font-size: 11px; -fx-cursor: hand; -fx-underline: true;");
                    btnReply.setOnAction(e -> {
                        selectedParentId = c.getId();
                        selectedCommentForEdit = null;
                        txtNewComment.setText("");
                        txtNewComment.setPromptText("Répondre à " + name + "...");
                        btnPostComment.setText("Répondre");
                        txtNewComment.requestFocus();
                    });
                    actions.getChildren().add(btnReply);
                    // Translation button
                    Button btnTranslateComment = new Button("Traduire 🌍");
                    btnTranslateComment.setStyle("-fx-background-color: transparent; -fx-text-fill: #3498db; -fx-font-size: 11px; -fx-cursor: hand; -fx-underline: true;");
                    
                    final String originalContent = c.getContenu();
                    final boolean[] isTranslated = {false};
                    
                    btnTranslateComment.setOnAction(e -> {
                        if (isTranslated[0]) {
                            content.setText(originalContent);
                            btnTranslateComment.setText("Traduire 🌍");
                            isTranslated[0] = false;
                        } else {
                            String target = comboLanguage.getValue() != null ? comboLanguage.getValue() : "français";
                            // Run in background to avoid freezing UI
                            new Thread(() -> {
                                String trans = services.TranslationService.translate(originalContent, target);
                                javafx.application.Platform.runLater(() -> {
                                    content.setText(trans);
                                    btnTranslateComment.setText("Original");
                                    isTranslated[0] = true;
                                });
                            }).start();
                        }
                    });
                    actions.getChildren().add(btnTranslateComment);
                    
                    // Show edit/delete actions only for the logged-in user (hardcoded as 1 right now)
                    if (c.getAuteurId() == 1) {
                        Button btnEditComment = new Button("Modifier");
                        btnEditComment.getStyleClass().add("btn-secondary");
                        btnEditComment.setStyle("-fx-font-size: 11px; -fx-padding: 3 8;");
                        btnEditComment.setOnAction(e -> {
                            selectedCommentForEdit = c;
                            selectedParentId = null;
                            txtNewComment.setText(c.getContenu());
                            btnPostComment.setText("Mettre à jour");
                            txtNewComment.requestFocus();
                        });

                        Button btnDeleteComment = new Button("Supprimer");
                        btnDeleteComment.getStyleClass().add("btn-danger");
                        btnDeleteComment.setStyle("-fx-font-size: 11px; -fx-padding: 3 8;");
                        btnDeleteComment.setOnAction(e -> {
                            sc.delete(c);
                            loadComments();
                        });
                        
                        actions.getChildren().addAll(btnEditComment, btnDeleteComment);
                    }

                    card.getChildren().add(actions);

                    HBox wrapper = new HBox();
                    if (c.getParentId() != null && c.getParentId() > 0) {
                        Region spacerIndent = new Region();
                        spacerIndent.setPrefWidth(40);
                        wrapper.getChildren().add(spacerIndent);
                    }
                    wrapper.getChildren().add(card);
                    HBox.setHgrow(card, Priority.ALWAYS);

                    setGraphic(wrapper);
                    setText(null);
                }
            }
        });
    }

    public void initData(Publication p) {
        this.currentPublication = p;
        lblTitre.setText(p.getTitre());
        lblType.setText("Type: " + p.getType());
        lblDate.setText("Publié le: " + p.getDateCreation());
        lblContenu.setText(p.getContenu());
        
        originalPublicationContent = p.getContenu();
        isPublicationTranslated = false;
        if(btnTranslateMain != null) btnTranslateMain.setText("Voir la traduction \uD83C\uDF0D");

        // Handle Image loading (Support for local files and Web URLs)
        if (p.getImagePath() != null && !p.getImagePath().isEmpty()) {
            try {
                String path = p.getImagePath();
                Image image;
                if (path.startsWith("http")) {
                    image = new Image(path, true); // true for background loading
                } else {
                    java.io.File file = new java.io.File(path);
                    if (file.exists()) {
                        image = new Image(file.toURI().toString());
                    } else {
                        image = null;
                    }
                }
                
                if (image != null) {
                    imgPublication.setImage(image);
                    imageContainer.setVisible(true);
                    imageContainer.setManaged(true);
                } else {
                    imageContainer.setVisible(false);
                    imageContainer.setManaged(false);
                }
            } catch (Exception e) {
                System.err.println("Error loading image: " + e.getMessage());
                imageContainer.setVisible(false);
                imageContainer.setManaged(false);
            }
        } else {
            imageContainer.setVisible(false);
            imageContainer.setManaged(false);
        }

        // Handle PDF loading
        if (p.getPdfPath() != null && !p.getPdfPath().isEmpty()) {
            java.io.File file = new java.io.File(p.getPdfPath());
            if (file.exists()) {
                btnOpenPdf.setVisible(true);
                btnOpenPdf.setManaged(true);
            } else {
                btnOpenPdf.setVisible(false);
                btnOpenPdf.setManaged(false);
            }
        } else {
            btnOpenPdf.setVisible(false);
            btnOpenPdf.setManaged(false);
        }

        loadComments();
        refreshPublicationReactions();
    }

    /** Refresh publication like/dislike counts and highlight active button */
    private void refreshPublicationReactions() {
        int likes    = sr.countLikes("publication", currentPublication.getId());
        int dislikes = sr.countDislikes("publication", currentPublication.getId());
        lblLikesPublication.setText(String.valueOf(likes));
        lblDislikesPublication.setText(String.valueOf(dislikes));

        String userReaction = sr.getUserReaction(activeUserId, "publication", currentPublication.getId());
        // Highlight active button
        btnLikePublication.getStyleClass().removeAll("btn-like-active", "btn-dislike-active");
        btnDislikePublication.getStyleClass().removeAll("btn-like-active", "btn-dislike-active");
        if ("like".equals(userReaction)) {
            btnLikePublication.getStyleClass().add("btn-like-active");
        } else if ("dislike".equals(userReaction)) {
            btnDislikePublication.getStyleClass().add("btn-dislike-active");
        }
    }

    @FXML
    private void likePublication(ActionEvent event) {
        sr.toggleReaction(activeUserId, "publication", currentPublication.getId(), "like");
        refreshPublicationReactions();
    }

    @FXML
    private void dislikePublication(ActionEvent event) {
        sr.toggleReaction(activeUserId, "publication", currentPublication.getId(), "dislike");
        refreshPublicationReactions();
    }

    private void loadComments() {
        obsList.clear();
        java.util.List<Commentaire> allComments = sc.getByPublication(currentPublication.getId());
        java.util.List<Commentaire> structuredList = new java.util.ArrayList<>();

        for (Commentaire c : allComments) {
            if (c.getParentId() == null || c.getParentId() == 0) {
                addCommentWithReplies(c, allComments, structuredList);
            }
        }

        obsList.addAll(structuredList);
        listCommentaires.setItems(obsList);
    }

    private void addCommentWithReplies(Commentaire current, java.util.List<Commentaire> allComments, java.util.List<Commentaire> structuredList) {
        structuredList.add(current);
        for (Commentaire reply : allComments) {
            if (reply.getParentId() != null && reply.getParentId().equals(current.getId())) {
                addCommentWithReplies(reply, allComments, structuredList);
            }
        }
    }

    @FXML
    private void toggleEditPublication(ActionEvent event) {
        boolean isEditing = !txtEditTitre.isVisible();

        // Toggle visibility and management
        lblTitre.setVisible(!isEditing);
        lblTitre.setManaged(!isEditing);
        lblContenu.setVisible(!isEditing);
        lblContenu.setManaged(!isEditing);

        txtEditTitre.setVisible(isEditing);
        txtEditTitre.setManaged(isEditing);
        txtEditContenu.setVisible(isEditing);
        txtEditContenu.setManaged(isEditing);

        btnEdit.setVisible(!isEditing);
        btnEdit.setManaged(!isEditing);
        btnSave.setVisible(isEditing);
        btnSave.setManaged(isEditing);
        btnCancel.setVisible(isEditing);
        btnCancel.setManaged(isEditing);

        if (isEditing) {
            txtEditTitre.setText(currentPublication.getTitre());
            txtEditContenu.setText(currentPublication.getContenu());
        }
    }

    @FXML
    private void savePublicationUpdate(ActionEvent event) {
        String newTitre = txtEditTitre.getText().trim();
        String newContenu = txtEditContenu.getText().trim();

        if (newTitre.isEmpty() || newContenu.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Le titre et le contenu ne peuvent pas être vides.");
            alert.show();
            return;
        }

        currentPublication.setTitre(newTitre);
        currentPublication.setContenu(newContenu);

        sp.update(currentPublication);
        
        // Update Labels
        lblTitre.setText(newTitre);
        lblContenu.setText(newContenu);
        
        // Return to view mode
        toggleEditPublication(event);
    }

    @FXML
    private void postComment(ActionEvent event) {
        String texte = txtNewComment.getText().trim();
        if (texte.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setContentText("Veuillez écrire un commentaire.");
            alert.show();
            return;
        }

        // If editing an existing comment — no hate check needed, just update
        if (selectedCommentForEdit != null) {
            selectedCommentForEdit.setContenu(texte);
            sc.update(selectedCommentForEdit);
            selectedCommentForEdit = null;
            btnPostComment.setText("Publier");
            txtNewComment.setPromptText("Apportez votre expertise...");
            txtNewComment.clear();
            loadComments();
            return;
        }

        // New comment → run hate speech check in background
        btnPostComment.setDisable(true);
        final Integer parentIdToUse = selectedParentId;

        new Thread(() -> {
            HateSpeechService.Result result = new HateSpeechService().analyze(texte);
            javafx.application.Platform.runLater(() -> {
                btnPostComment.setDisable(false);
                if (result.toxic) {
                    String reason = result.reason.isEmpty() ? "contenu inapproprié"
                            : result.reason.replace("_", " ").toLowerCase();
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("🚫 Commentaire bloqué");
                    alert.setHeaderText("Discours haineux détecté");
                    alert.setContentText(
                            "Votre commentaire contient du contenu inapproprié ("
                            + reason + ", score: "
                            + String.format("%.0f", result.score * 100) + "%)."
                            + "\n\nVeuillez reformuler votre message.");
                    alert.show();
                } else {
                    Commentaire c = new Commentaire();
                    c.setContenu(texte);
                    c.setPublicationId(currentPublication.getId());
                    c.setAuteurId(activeUserId);
                    if (parentIdToUse != null) {
                        c.setParentId(parentIdToUse);
                        selectedParentId = null;
                    }
                    sc.add(c);
                    // Gamification: Increment user actions for forum comment
                    new services.UserService().incrementActionsCount(activeUserId);
                    btnPostComment.setText("Publier");
                    txtNewComment.setPromptText("Apportez votre expertise...");
                    txtNewComment.clear();
                    loadComments();
                }
            });
        }).start();
    }




    @FXML
    private void translateMainAction(ActionEvent event) {
        if (isPublicationTranslated) {
            lblContenu.setText(originalPublicationContent);
            btnTranslateMain.setText("Voir la traduction \uD83C\uDF0D");
            isPublicationTranslated = false;
        } else {
            String targetLang = comboLanguage.getValue() != null ? comboLanguage.getValue() : "français";
            btnTranslateMain.setText("Traduction...");
            new Thread(() -> {
                String translated = services.TranslationService.translate(originalPublicationContent, targetLang);
                javafx.application.Platform.runLater(() -> {
                    lblContenu.setText(translated);
                    btnTranslateMain.setText("Afficher l'original");
                    isPublicationTranslated = true;
                });
            }).start();
        }
    }

    @FXML
    private void openPdf(ActionEvent event) {
        if (currentPublication.getPdfPath() != null && !currentPublication.getPdfPath().isEmpty()) {
            try {
                java.io.File file = new java.io.File(currentPublication.getPdfPath());
                if (file.exists() && java.awt.Desktop.isDesktopSupported()) {
                    java.awt.Desktop.getDesktop().open(file);
                } else {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setContentText("Impossible d'ouvrir le fichier PDF. Il est peut-être introuvable ou votre système ne supporte pas cette action.");
                    alert.show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        utils.UserSession.getInstance().cleanUserSession();
        utils.NavigationService.switchScene(event, "/esprit/tn/fxml/home.fxml", "Bienvenue");
    }

    @FXML
    private void handleProfil(ActionEvent event) {
        utils.NavigationService.switchScene(event, "/esprit/tn/fxml/profile.fxml", "Mon Profil");
    }

    @FXML
    private void handleEvenements(javafx.event.ActionEvent event) {
        utils.NavigationService.switchScene(event, "/esprit/tn/fxml/front_evenements.fxml", "Événements");
    }

    @FXML
    private void handleDons(javafx.event.ActionEvent event) {
        utils.NavigationService.switchScene(event, "/esprit/tn/fxml/admin_donations_offres.fxml", "Dons");
    }

    @FXML
    private void handleAccueil(ActionEvent event) {
        utils.NavigationService.switchScene(event, "/esprit/tn/fxml/front.fxml", "Accueil");
    }

    @FXML
    private void goBack(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/esprit/tn/fxml/forum.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 900, 600));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
