package controllers;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import models.Evenement;
import services.EvenementService;
import models.User;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import utils.AlertUtils;
import utils.NavigationService;
import utils.UserSession;
import java.io.IOException;

public class FrontEvenementsController {

    @FXML private FlowPane eventContainer;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> sortCombo;

    private final EvenementService evenementService = new EvenementService();
    private List<Evenement> allEvents = new ArrayList<>();

    @FXML
    public void initialize() {
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
            Label dateLabel = (Label) card.lookup("#eventDate");
            Label locLabel = (Label) card.lookup("#eventLocation");
            ImageView imgView = (ImageView) card.lookup("#eventImage");
            Button btn = (Button) card.lookup("#participerBtn");

            // Set data
            nameLabel.setText(ev.getNom());
            dateLabel.setText(ev.getDateEvenement().toLocalDate().toString());
            locLabel.setText(ev.getLieuVille() + ", " + ev.getLieuAdresse());

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
}
