import services.LieuService;
import models.Lieu;
import java.util.List;

public class CheckLieux {
    public static void main(String[] args) {
        LieuService ls = new LieuService();
        List<Lieu> lieux = ls.getAll();
        System.out.println("COUNT_LIEUX:" + lieux.size());
        for (Lieu l : lieux) {
            System.out.println("LIEU_ID:" + l.getId() + " - " + l.getVille());
        }
    }
}
