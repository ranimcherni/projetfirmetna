package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import models.Evenement;
import utils.AlertUtils;
import utils.NavigationService;
import utils.QRCodeGenerator;
import utils.UserSession;
import services.UserService;
import javafx.scene.image.WritableImage;

public class EvenementDetailsController {

    @FXML private ImageView eventImage;
    @FXML private Label eventTitle;
    @FXML private Label eventDate;
    @FXML private Label eventLocation;
    @FXML private Label eventOrganisateur;
    @FXML private Label eventDescription;
    @FXML private Label participantCount;
    @FXML private ImageView qrCodeImage;
    @FXML private VBox detailsContainer;

    private Evenement currentEvent;

    public void setEvent(Evenement event) {
        this.currentEvent = event;
        
        // Titre et Date
        eventTitle.setText(event.getNom());
        eventDate.setText("📅 " + event.getDateEvenement().toString());
        
        // Lieu
        String lieu = (event.getLieuVille() != null ? event.getLieuVille() : "") 
                    + (event.getLieuAdresse() != null ? " - " + event.getLieuAdresse() : "");
        eventLocation.setText("📍 " + (lieu.isEmpty() ? "Lieu non précisé" : lieu));
        
        // Organisateur
        eventOrganisateur.setText("👤 Organisé par : " + (event.getOrganisateur() != null ? event.getOrganisateur() : "Anonyme"));
        
        // Description
        eventDescription.setText(event.getDescription());
        
        // Capacité (statique pour l'instant ou via service)
        participantCount.setText("👥 Capacité : " + event.getLieuCapacite() + " personnes");

        // Image de l'événement
        try {
            if (event.getImage() != null && !event.getImage().isEmpty()) {
                eventImage.setImage(new Image(event.getImage(), true));
            } else {
                eventImage.setImage(new Image(getClass().getResource("/esprit/tn/images/logo1.png").toExternalForm()));
            }
        } catch (Exception e) {
            eventImage.setImage(new Image(getClass().getResource("/esprit/tn/images/logo1.png").toExternalForm()));
        }

        // Génération du QR Code
        generateQRCode();
    }

    private void generateQRCode() {
        if (currentEvent == null) return;

        // On encode les infos principales dans le QR Code
        String qrData = "Événement : " + currentEvent.getNom() + "\n"
                      + "Date : " + currentEvent.getDateEvenement() + "\n"
                      + "Lieu : " + currentEvent.getLieuVille() + "\n"
                      + "Organisateur : " + currentEvent.getOrganisateur();

        WritableImage qrImage = QRCodeGenerator.generateQRCodeImage(qrData, 200, 200);
        if (qrImage != null) {
            qrCodeImage.setImage(qrImage);
        }
    }

    @FXML
    private void handleBack(ActionEvent event) {
        NavigationService.switchScene(event, "/esprit/tn/fxml/front_evenements.fxml", "Événements");
    }

    @FXML
    private void handleParticiper(ActionEvent event) {
        if (UserSession.getInstance().getUser() == null) {
            AlertUtils.showError("Connexion requise", "Veuillez vous connecter pour participer à cet événement.");
            return;
        }

        int userId = UserSession.getInstance().getUser().getId();
        services.EvenementService service = new services.EvenementService();
        services.EvenementService.ReservationStatus status = service.reserverPlace(currentEvent.getId(), userId);

        switch (status) {
            case SUCCESS:
                AlertUtils.showSuccess("Succès", "Votre participation à l'événement '" + currentEvent.getNom() + "' a été enregistrée !");
                // Gamification: Increment user actions for event participation
                new UserService().incrementActionsCount(userId);
                break;
            case ALREADY_PARTICIPATING:
                AlertUtils.showError("Déjà inscrit", "Vous participez déjà à cet événement.");
                break;
            case FULL:
                AlertUtils.showError("Événement complet", "Désolé, cet événement a atteint sa capacité maximale.");
                break;
            case ERROR:
                AlertUtils.showError("Erreur", "Une erreur est survenue lors de l'enregistrement de votre participation.");
                break;
        }
    }
}
