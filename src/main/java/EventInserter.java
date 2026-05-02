import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EventInserter {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3307/firmetna_new_db";
        String user = "root";
        String password = "";

        try (Connection cnx = DriverManager.getConnection(url, user, password)) {
            System.out.println("Connected to DB.");

            // Check for existing Lieux
            Statement st = cnx.createStatement();
            ResultSet rs = st.executeQuery("SELECT id, ville FROM lieu");
            List<Integer> lieuIds = new ArrayList<>();
            while (rs.next()) {
                lieuIds.add(rs.getInt("id"));
            }

            if (lieuIds.isEmpty()) {
                System.out.println("No Lieux found. Creating default Lieux...");
                String[] villes = {"Bizerte", "Sousse", "Tunis", "Beja", "Kef"};
                for (String ville : villes) {
                    PreparedStatement ps = cnx.prepareStatement("INSERT INTO lieu (adresse, ville, capacite, disponibilite, description) VALUES (?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
                    ps.setString(1, "Adresse " + ville);
                    ps.setString(2, ville);
                    ps.setInt(3, 100);
                    ps.setBoolean(4, true);
                    ps.setString(5, "Lieu pour événements à " + ville);
                    ps.executeUpdate();
                    ResultSet generatedKeys = ps.getGeneratedKeys();
                    if (generatedKeys.next()) {
                        lieuIds.add(generatedKeys.getInt(1));
                    }
                }
            }

            // Insert 5 events
            String[] noms = {
                "Grande Foire Agricole Firmetna",
                "Formation Permaculture & Design",
                "Fête de la Récolte d'Automne",
                "Atelier Artisanal : Fabrication de Fromage",
                "Concours National du Meilleur Élevage"
            };
            String[] descriptions = {
                "Venez découvrir les dernières innovations agricoles et les produits de nos terroirs.",
                "Apprenez les bases de la permaculture pour une agriculture durable et respectueuse.",
                "Célébrons ensemble l'abondance de la saison avec des dégustations et des animations.",
                "Un atelier pratique pour maîtriser l'art de la transformation laitière traditionnelle.",
                "Présentation des plus beaux spécimens de nos fermes partenaires."
            };
            String[] images = {
                "/esprit/tn/images/events/foire.png",
                "/esprit/tn/images/events/permaculture.png",
                "/esprit/tn/images/events/recolte.png",
                "/esprit/tn/images/events/fromagerie.png",
                "/esprit/tn/images/events/elevage.png"
            };
            String organisateur = "Administration Firmetna";
            Date dateEvenement = Date.valueOf("2026-06-15");

            PreparedStatement psInsert = cnx.prepareStatement("INSERT INTO evenement (lieu_id, nom, description, date_evenement, organisateur, image) VALUES (?, ?, ?, ?, ?, ?)");
            
            for (int i = 0; i < 5; i++) {
                int lieuId = lieuIds.get(i % lieuIds.size());
                psInsert.setInt(1, lieuId);
                psInsert.setString(2, noms[i]);
                psInsert.setString(3, descriptions[i]);
                psInsert.setDate(4, new java.sql.Date(dateEvenement.getTime() + (long)i * 7 * 24 * 60 * 60 * 1000)); // One week apart
                psInsert.setString(5, organisateur);
                psInsert.setString(6, images[i]);
                psInsert.executeUpdate();
            }

            System.out.println("5 events created successfully in table 'evenement'!");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
