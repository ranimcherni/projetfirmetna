package controllers;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
<<<<<<< HEAD
=======
import javafx.scene.Scene;
>>>>>>> gestion-user
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
<<<<<<< HEAD
import models.Evenement;
import services.EvenementService;
import models.User;
=======
import javafx.stage.Stage;
import models.Evenement;
import services.EvenementService;
import models.User;
import java.io.IOException;
>>>>>>> gestion-user
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import utils.AlertUtils;
import utils.NavigationService;
import utils.UserSession;
<<<<<<< HEAD
import java.io.IOException;
=======
>>>>>>> gestion-user

public class FrontEvenementsController {

    @FXML private FlowPane eventContainer;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> sortCombo;

    private final EvenementService evenementService = new EvenementService();
    private List<Evenement> allEvents = new ArrayList<>();

    @FXML
    public void initialize() {
<<<<<<< HEAD
        allEvents = evenementService.getAll();
        
        setupSortCombo();
        setupSearchListener();
        
        loadEvents(allEvents);
    }

    private void setupSortCombo() {
        sortCombo.setItems(FXCollections.observableArrayList(
                "Plus récents",
                "Plus anciens",
                "Nom (A-Z)",
                "Nom (Z-A)"
        ));
        sortCombo.getSelectionModel().select(0);
        sortCombo.setOnAction(e -> filterAndSort());
    }

    private void setupSearchListener() {
        searchField.textProperty().addListener((obs, oldVal, newVal) -> filterAndSort());
    }

    private void filterAndSort() {
        String searchText = searchField.getText().toLowerCase().trim();
        String sortOption = sortCombo.getSelectionModel().getSelectedItem();

        // Filter
        List<Evenement> filtered = allEvents.stream()
                .filter(ev -> ev.getNom().toLowerCase().contains(searchText))
                .collect(Collectors.toList());

        // Sort
        if (sortOption != null) {
            switch (sortOption) {
                case "Plus récents":
                    filtered.sort(Comparator.comparing(Evenement::getDateEvenement).reversed());
                    break;
                case "Plus anciens":
                    filtered.sort(Comparator.comparing(Evenement::getDateEvenement));
                    break;
                case "Nom (A-Z)":
                    filtered.sort(Comparator.comparing(ev -> ev.getNom().toLowerCase()));
                    break;
                case "Nom (Z-A)":
                    filtered.sort(Comparator.comparing((Evenement ev) -> ev.getNom().toLowerCase()).reversed());
                    break;
            }
        }

        loadEvents(filtered);
    }

    private void loadEvents(List<Evenement> events) {
        eventContainer.getChildren().clear();
        for (Evenement ev : events) {
            addEventCard(ev);
        }
    }

    private void addEventCard(Evenement ev) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/esprit/tn/fxml/evenement_card.fxml"));
            Parent card = loader.load();
            
            // Access components from the card
            Label nameLabel = (Label) card.lookup("#eventName");
            ImageView imgView = (ImageView) card.lookup("#eventImage");
            ImageView qrCodeView = (ImageView) card.lookup("#qrCodeImage");
            Button btn = (Button) card.lookup("#participerBtn");
            Button detailsBtn = (Button) card.lookup("#detailsBtn");
            Label dateLabel = (Label) card.lookup("#eventDate");
            Label locLabel = (Label) card.lookup("#eventLocation");

            // Set data
            nameLabel.setText(ev.getNom());
            dateLabel.setText(ev.getDateEvenement().toLocalDate().toString());
            locLabel.setText(ev.getLieuVille() + ", " + ev.getLieuAdresse());

            // Prepare QR Code Content with UTF-8 BOM (\uFEFF) to force correct encoding on mobile scanners
            String qrText = "\uFEFF" + String.format("Événement : %s\nDate : %s\nLieu : %s, %s\nOrganisateur : %s\n\nDescription :\n%s",
                    ev.getNom(),
                    ev.getDateEvenement().toLocalDate().toString(),
                    ev.getLieuVille(), ev.getLieuAdresse(),
                    ev.getOrganisateur() != null ? ev.getOrganisateur() : "Non spécifié",
                    ev.getDescription());

            // Generate and Set QR Code
            javafx.scene.image.WritableImage qrImage = utils.QRCodeGenerator.generateQRCodeImage(qrText, 150, 150);
            if (qrImage != null) {
                qrCodeView.setImage(qrImage);
            }

            // Set image
            if (ev.getImage() != null && !ev.getImage().isEmpty()) {
                try {
                    imgView.setImage(new Image(ev.getImage(), true));
                } catch (Exception e) {
                    setDefaultImage(imgView);
                }
            } else {
                setDefaultImage(imgView);
            }

            // Etat réservation selon participants/capacité
            refreshReservationButton(btn, ev);
            btn.setOnAction(e -> participer(ev, btn));

            // Setup details button
            detailsBtn.setOnAction(e -> {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Détails de l'événement");
                alert.setHeaderText(ev.getNom());
                
                // Remove BOM before displaying in UI
                String displayTxt = qrText.replace("\uFEFF", "");
                
                TextArea textArea = new TextArea(displayTxt);
                textArea.setEditable(false);
                textArea.setWrapText(true);
                textArea.setMaxWidth(Double.MAX_VALUE);
                textArea.setMaxHeight(Double.MAX_VALUE);
                
                alert.getDialogPane().setContent(textArea);
                alert.showAndWait();
            });
            eventContainer.getChildren().add(card);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setDefaultImage(ImageView img) {
        try {
            img.setImage(new Image(getClass().getResourceAsStream("/esprit/tn/images/logo1.png")));
        } catch (Exception e) {
            // Silently fail if logo also missing
        }
    }


    private void refreshReservationButton(Button btn, Evenement evenement) {
        boolean complet = evenementService.isEvenementComplet(evenement.getId());
        if (complet) {
            btn.setText("Complet");
            btn.setDisable(true);
            return;
        }

        User currentUser = UserSession.getInstance().getUser();
        if (currentUser != null) {
            if (evenementService.hasParticipation(evenement.getId(), currentUser.getId())) {
                btn.setText("Deja inscrit");
                btn.setDisable(true);
                return;
            }
        }

        btn.setText("Participer");
        btn.setDisable(false);
    }

    private void participer(Evenement e, Button btn) {
        User currentUser = UserSession.getInstance().getUser();
        if (currentUser == null) {
            AlertUtils.showError("Participation", "Connectez-vous pour participer.");
            return;
        }

        EvenementService.ReservationStatus status = evenementService.reserverPlace(e.getId(), currentUser.getId());
        switch (status) {
            case SUCCESS:
                AlertUtils.showSuccess("Participation",
                        "Votre participation est enregistree pour : " + e.getNom());
                break;
            case FULL:
                AlertUtils.showError("Participation", "Cet evenement est complet.");
                break;
            case ALREADY_PARTICIPATING:
                AlertUtils.showError("Participation", "Vous participez deja a cet evenement.");
                break;
            default:
                AlertUtils.showError("Participation", "Erreur lors de la participation.");
                break;
        }
        refreshReservationButton(btn, e);
    }
=======
        sortCombo.setItems(FXCollections.observableArrayList("Plus récents", "Plus anciens"));
        sortCombo.setValue("Plus récents");

        loadEvents();

        searchField.textProperty().addListener((obs, oldV, newV) -> filterAndSort());
        sortCombo.valueProperty().addListener((obs, oldV, newV) -> filterAndSort());
    }

    private void loadEvents() {
        allEvents = evenementService.getAll();
        filterAndSort();
    }

    private void filterAndSort() {
        String search = searchField.getText().toLowerCase();
        List<Evenement> filtered = allEvents.stream()
                .filter(e -> e.getNom().toLowerCase().contains(search) || e.getDescription().toLowerCase().contains(search))
                .collect(Collectors.toList());

        String sort = sortCombo.getValue();
        if ("Plus récents".equals(sort)) {
            filtered.sort(Comparator.comparing(Evenement::getDateEvenement).reversed());
        } else if ("Plus anciens".equals(sort)) {
            filtered.sort(Comparator.comparing(Evenement::getDateEvenement));
        }

        renderEvents(filtered);
    }

    private void renderEvents(List<Evenement> events) {
        eventContainer.getChildren().clear();
        for (Evenement event : events) {
            eventContainer.getChildren().add(createEventCard(event));
        }
    }

    private VBox createEventCard(Evenement event) {
        VBox card = new VBox(15);
        card.getStyleClass().add("glass-card");
        card.setPrefWidth(280);
        card.setStyle("-fx-padding: 20; -fx-background-radius: 20;");

        ImageView imageView = new ImageView();
        try {
            if (event.getImage() != null && !event.getImage().isEmpty()) {
                imageView.setImage(new Image(event.getImage(), true));
            } else {
                imageView.setImage(new Image(getClass().getResource("/esprit/tn/images/logo1.png").toExternalForm()));
            }
        } catch (Exception e) {
            imageView.setImage(new Image(getClass().getResource("/esprit/tn/images/logo1.png").toExternalForm()));
        }
        imageView.setFitWidth(240);
        imageView.setFitHeight(150);
        imageView.setPreserveRatio(true);
        imageView.getStyleClass().add("event-card-image");

        Label title = new Label(event.getNom());
        title.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: #1b4332;");
        title.setWrapText(true);

        Label date = new Label("📅 " + event.getDateEvenement().toString());
        date.setStyle("-fx-text-fill: #2d5a27; -fx-font-weight: bold;");

        Label location = new Label("📍 " + (event.getLieuVille() != null ? event.getLieuVille() : "Lieu non précisé"));
        location.setStyle("-fx-text-fill: #666; -fx-font-size: 13;");

        Button detailsBtn = new Button("Voir Détails");
        detailsBtn.setStyle("-fx-background-color: #2d5a27; -fx-text-fill: white; -fx-background-radius: 10; -fx-padding: 8 20; -fx-cursor: hand;");
        detailsBtn.setMaxWidth(Double.MAX_VALUE);
        detailsBtn.setOnAction(e -> showEventDetails(event));

        card.getChildren().addAll(imageView, title, date, location, detailsBtn);
        
        // Hover effect
        card.setOnMouseEntered(e -> card.setStyle("-fx-padding: 20; -fx-background-radius: 20; -fx-background-color: rgba(255,255,255,0.9); -fx-scale-x: 1.02; -fx-scale-y: 1.02;"));
        card.setOnMouseExited(e -> card.setStyle("-fx-padding: 20; -fx-background-radius: 20; -fx-background-color: rgba(255,255,255,0.75); -fx-scale-x: 1; -fx-scale-y: 1;"));

        return card;
    }

    private void showEventDetails(Evenement event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/esprit/tn/fxml/evenement_details.fxml"));
            Parent root = loader.load();
            
            EvenementDetailsController controller = loader.getController();
            controller.setEvent(event);
            
            Stage stage = new Stage();
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.initOwner(eventContainer.getScene().getWindow());
            stage.setTitle("Détails de l'événement - " + event.getNom());
            stage.setScene(new javafx.scene.Scene(root));
            stage.showAndWait();
            
        } catch (IOException e) {
            e.printStackTrace();
            AlertUtils.showError("Erreur", "Impossible d'ouvrir les détails de l'événement.");
        }
    }

>>>>>>> gestion-user

    @FXML
    private void handleAccueil(ActionEvent event) {
        NavigationService.switchScene(event, "/esprit/tn/fxml/front.fxml", "Accueil");
    }

    @FXML
    private void handleProfil(ActionEvent event) {
        if (UserSession.getInstance().getUser() == null) {
            NavigationService.switchScene(event, "/esprit/tn/fxml/home.fxml", "Bienvenue");
            return;
        }
        NavigationService.switchScene(event, "/esprit/tn/fxml/profile.fxml", "Mon Profil");
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        UserSession.getInstance().cleanUserSession();
        NavigationService.switchScene(event, "/esprit/tn/fxml/home.fxml", "Bienvenue");
    }
<<<<<<< HEAD
=======

    @FXML
    private void handleProduits(ActionEvent event) {
        NavigationService.switchScene(event, "/esprit/tn/fxml/product_marketplace.fxml", "Produits");
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
    private void handleForum(ActionEvent event) {
        NavigationService.switchScene(event, "/esprit/tn/fxml/forum.fxml", "Forum");
    }

    @FXML
    private void handleDons(ActionEvent event) {
        NavigationService.switchScene(event, "/esprit/tn/fxml/front_donations_offres.fxml", "Donations & Solidarité");
    }

    @FXML
    private void handlePartenariats(ActionEvent event) {
        NavigationService.switchScene(event, "/esprit/tn/fxml/FrontPartnerView.fxml", "Espace Partenaires");
    }

    @FXML
    private void handleOpenChatbot(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/esprit/tn/fxml/chatbot.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Chatbot Firmetna");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
>>>>>>> gestion-user
}
