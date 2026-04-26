package controllers;

import models.Demande;
import models.Offre;
import services.DemandeService;
import services.OffreService;
import services.UserService;

import models.User;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;

import java.util.List;

public class DonationController {
    private final OffreService offreService;
    private final DemandeService demandeService;
    private final UserService userService;

    public DonationController() {
        this.offreService = new OffreService();
        this.demandeService = new DemandeService();
        this.userService = new UserService();
    }

    // CRUD Offre
    public void createOffre(Offre offre) {
        offreService.add(offre);
    }

    public void updateOffre(Offre offre) {
        offreService.update(offre);
    }

    public void deleteOffre(Offre offre) {
        offreService.delete(offre);
    }

    public List<Offre> getAllOffres() {
        return offreService.getAll();
    }

    public Offre getOffreById(int id) {
        return offreService.getById(id);
    }

    // CRUD Demande
    public boolean createDemande(Demande demande) {
        return demandeService.addWithValidation(demande);
    }

    public void updateDemande(Demande demande) {
        demandeService.update(demande);
    }

    public void deleteDemande(Demande demande) {
        demandeService.delete(demande);
    }

    public List<Demande> getAllDemandes() {
        return demandeService.getAll();
    }

    public Demande getDemandeById(int id) {
        return demandeService.getById(id);
    }

    // Integration simplifiee avec la tache User: creation d'une demande par IDs.
    public boolean createDemande(int offreId, int userId, int quantiteDemandee, String statut, boolean disponible) {
        return createDemande(offreId, userId, quantiteDemandee, statut, disponible, null);
    }

    public boolean createDemande(int offreId, int userId, int quantiteDemandee, String statut, boolean disponible, LocalDateTime dateRecuperation) {
        Offre offre = offreService.getById(offreId);
        User user = userService.getById(userId);
        if (offre == null || user == null) {
            return false;
        }

        Demande demande = new Demande();
        demande.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        demande.setStatut(statut != null ? statut : "EN_ATTENTE");
        demande.setOffre(offre);
        demande.setDemandeur(user);
        demande.setQuantiteDemandee(quantiteDemandee);
        demande.setDisponible(disponible);
        demande.setDateRecuperation(dateRecuperation);
        return demandeService.addWithValidation(demande);
    }

    public boolean updateDateRecuperation(int demandeId, LocalDateTime dateRecuperation) {
        return demandeService.updateDateRecuperation(demandeId, dateRecuperation);
    }

    public List<CalendarEventDto> getCalendarRecuperations() {
        List<CalendarEventDto> events = new ArrayList<>();
        for (Demande demande : demandeService.getAllRecuperations()) {
            if (!demandeService.isDemandeValide(demande) || !demandeService.isDateRecuperationValide(demande.getDateRecuperation())) {
                continue;
            }
            events.add(new CalendarEventDto(
                    demande.getId(),
                    "Recuperation donation",
                    demande.getDateRecuperation() != null ? demande.getDateRecuperation().toString() : null,
                    demande.getStatut()
            ));
        }
        return events;
    }

    public static class CalendarEventDto {
        private final int id;
        private final String titre;
        private final String dateRecuperation;
        private final String statut;

        public CalendarEventDto(int id, String titre, String dateRecuperation, String statut) {
            this.id = id;
            this.titre = titre;
            this.dateRecuperation = dateRecuperation;
            this.statut = statut;
        }

        public int getId() {
            return id;
        }

        public String getTitre() {
            return titre;
        }

        public String getDateRecuperation() {
            return dateRecuperation;
        }

        public String getStatut() {
            return statut;
        }
    }
}
