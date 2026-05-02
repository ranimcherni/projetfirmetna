package test;
import services.PartnerService;
import models.Partner;
import java.util.List;

public class PartnerLookup {
    public static void main(String[] args) {
        PartnerService ps = new PartnerService();
        List<Partner> partners = ps.getAll();
        System.out.println("--- Liste des Partenaires ---");
        for (Partner p : partners) {
            System.out.println("Nom: " + p.getName() + " | Email: " + p.getEmail());
        }
    }
}
