package utils;

import models.Contract;
import models.Partner;
import services.ContractService;
import services.PartnerService;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

public class DummyDataGenerator {

    public static void main(String[] args) {
        System.out.println("🚀 Démarrage de la génération des données de test...");

        PartnerService partnerService = new PartnerService();
        ContractService contractService = new ContractService();

        // 1. Création des Partenaires
        System.out.println("Création des partenaires...");
        Partner p1 = new Partner("AgriBio Tunisie", "Supplier", "contact@agribio.tn", "22334455", "Sfax, Tunisie");
        Partner p2 = new Partner("Carrefour Market", "Distributor", "achats@carrefour.tn", "71889900", "Tunis, Tunisie");
        Partner p3 = new Partner("GreenTech Solutions", "Donor", "invest@greentech.com", "55667788", "Sousse, Tunisie");

        // Insert partners
        if (partnerService.getPartnerByEmail(p1.getEmail()) == null) partnerService.add(p1);
        if (partnerService.getPartnerByEmail(p2.getEmail()) == null) partnerService.add(p2);
        if (partnerService.getPartnerByEmail(p3.getEmail()) == null) partnerService.add(p3);

        System.out.println("✅ Partenaires ajoutés avec succès !");

        // Récupération des IDs générés par la base de données
        List<Partner> allPartners = partnerService.getAll();
        int idAgriBio = -1, idCarrefour = -1, idGreenTech = -1;

        for (Partner p : allPartners) {
            if (p.getEmail().equals(p1.getEmail())) idAgriBio = p.getId();
            if (p.getEmail().equals(p2.getEmail())) idCarrefour = p.getId();
            if (p.getEmail().equals(p3.getEmail())) idGreenTech = p.getId();
        }

        // 2. Création des Contrats
        System.out.println("Création des contrats...");
        
        // Contrat 1 : Actif (se termine dans 30 jours)
        if (idAgriBio != -1) {
            Contract c1 = new Contract(
                    idAgriBio,
                    "Fourniture Semences Bio",
                    "Contrat annuel pour la fourniture de semences biologiques certifiées.",
                    Date.valueOf(LocalDate.now().minusDays(15)), // Commencé il y a 15 jours
                    Date.valueOf(LocalDate.now().plusDays(30)),  // Se termine dans 30 jours
                    15500.0,
                    "Actif"
            );
            contractService.add(c1);
        }

        // Contrat 2 : Expiré (s'est terminé il y a 5 jours)
        if (idCarrefour != -1) {
            Contract c2 = new Contract(
                    idCarrefour,
                    "Distribution Légumes Frais",
                    "Accord de distribution dans le Grand Tunis.",
                    Date.valueOf(LocalDate.now().minusDays(60)),
                    Date.valueOf(LocalDate.now().minusDays(5)), // Expiré il y a 5 jours
                    25000.5,
                    "Expiré"
            );
            contractService.add(c2);
        }

        // Contrat 3 : En attente
        if (idGreenTech != -1) {
            Contract c3 = new Contract(
                    idGreenTech,
                    "Subvention Système d'Irrigation",
                    "Aide financière pour l'installation d'un système goutte à goutte.",
                    Date.valueOf(LocalDate.now().plusDays(10)), // Commence dans 10 jours
                    Date.valueOf(LocalDate.now().plusDays(365)), // Dure 1 an
                    50000.0,
                    "En attente"
            );
            contractService.add(c3);
        }

        System.out.println("✅ Contrats ajoutés avec succès !");
        System.out.println("🎉 TOUT EST PRÊT ! Vous pouvez maintenant lancer l'application.");
    }
}
