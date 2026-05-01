package test;

import controllers.DonationController;
import models.Demande;
import models.Offre;
import models.User;
import services.UserService;

import java.util.List;

public class DonationCrudMain {
    public static void main(String[] args) {
        DonationController donationController = new DonationController();
        UserService userService = new UserService();

        try {
            System.out.println("\n--- [DON] 1) CREATION OFFRE ---");
            Offre offre = new Offre("22112211", "Vetements", "Lot de vetements en bon etat", "photo1.jpg", 10);
            donationController.createOffre(offre);

            List<Offre> offres = donationController.getAllOffres();
            if (offres.isEmpty()) {
                System.out.println("Aucune offre trouvee.");
                return;
            }
            Offre lastOffre = offres.get(0);
            System.out.println("Offre creee ID=" + lastOffre.getId() + " quantite=" + lastOffre.getQuantite());

            System.out.println("\n--- [DON] 2) RECUPERATION USER POUR DEMANDE ---");
            List<User> users = userService.getAll();
            if (users.isEmpty()) {
                System.out.println("Aucun user trouve. Cree d'abord un user pour tester les demandes.");
                return;
            }
            User demandeur = users.get(0);
            System.out.println("Demandeur: " + demandeur.getId() + " - " + demandeur.getEmail());

            System.out.println("\n--- [DON] 3) CREATION DEMANDE VALIDE ---");
            boolean ok = donationController.createDemande(lastOffre.getId(), demandeur.getId(), 3, "EN_ATTENTE", true);
            System.out.println("Demande valide creee ? " + ok);

            System.out.println("\n--- [DON] 4) CREATION DEMANDE INVALIDE (quantite > offre) ---");
            boolean ko = donationController.createDemande(lastOffre.getId(), demandeur.getId(), 99, "EN_ATTENTE", true);
            System.out.println("Demande invalide creee ? " + ko + " (attendu: false)");

            System.out.println("\n--- [DON] 5) LECTURE DEMANDES ---");
            List<Demande> demandes = donationController.getAllDemandes();
            for (Demande d : demandes) {
                System.out.println("Demande #" + d.getId()
                        + " offre=" + d.getOffre().getId()
                        + " user=" + d.getDemandeur().getId()
                        + " qte=" + d.getQuantiteDemandee()
                        + " statut=" + d.getStatut());
            }
        } catch (Exception e) {
            System.err.println("Erreur test Donation CRUD: " + e.getMessage());
        }
    }
}
