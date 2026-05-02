package test;

import models.Contract;
import models.Partner;
import services.ContractService;
import services.PartnerService;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

public class CreateContracts {
    public static void main(String[] args) {
        PartnerService ps = new PartnerService();
        ContractService cs = new ContractService();
        
        List<Partner> partners = ps.getAll();
        System.out.println("Found " + partners.size() + " partners.");
        
        for (Partner p : partners) {
            String title = "Offre Spéciale - " + p.getName();
            double value = 1000 + (Math.random() * 9000); // Random value between 1k and 10k
            Date start = Date.valueOf(LocalDate.now());
            Date end = Date.valueOf(LocalDate.now().plusMonths(6));
            
            String description = "Distribution et partenariat pour " + p.getName();
            Contract c = new Contract(p.getId(), title, description, start, end, value, "EN_COURS");
            cs.add(c);
            System.out.println("Added contract for: " + p.getName());
        }
        System.out.println("DONE.");
    }
}
