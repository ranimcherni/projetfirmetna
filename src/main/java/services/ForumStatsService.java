package services;

import utils.MyDataBase;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.LinkedHashMap;

public class ForumStatsService {
    private Connection cnx;

    public ForumStatsService() {
        cnx = MyDataBase.getInstance().getCnx();
    }

    /**
     * Gets the top users by total interactions (publications + comments + reactions).
     * @return A map of username to interaction count, ordered by count.
     */
    public Map<String, Integer> getUserInteractionStats() {
        Map<String, Integer> stats = new LinkedHashMap<>();
        String query = "SELECT u.nom, u.prenom, " +
                "((SELECT COUNT(*) FROM publication p WHERE p.auteur_id = u.id) + " +
                "(SELECT COUNT(*) FROM commentaire c WHERE c.auteur_id = u.id) + " +
                "(SELECT COUNT(*) FROM reaction r WHERE r.user_id = u.id)) AS total_interactions " +
                "FROM user u " +
                "HAVING total_interactions > 0 " +
                "ORDER BY total_interactions DESC " +
                "LIMIT 10";
        try (Statement stm = cnx.createStatement();
             ResultSet rs = stm.executeQuery(query)) {
            while (rs.next()) {
                String name = rs.getString("prenom") + " " + rs.getString("nom");
                stats.put(name, rs.getInt("total_interactions"));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching user interaction stats: " + e.getMessage());
        }
        return stats;
    }

    /**
     * Gets the global distribution of interaction types.
     * @return A map of type name to count.
     */
    public Map<String, Integer> getInteractionTypeStats() {
        Map<String, Integer> stats = new HashMap<>();
        try {
            Statement stm = cnx.createStatement();
            
            ResultSet rs = stm.executeQuery("SELECT COUNT(*) FROM publication");
            if (rs.next()) stats.put("Publications", rs.getInt(1));
            
            rs = stm.executeQuery("SELECT COUNT(*) FROM commentaire");
            if (rs.next()) stats.put("Commentaires", rs.getInt(1));
            
            rs = stm.executeQuery("SELECT COUNT(*) FROM reaction WHERE type='like'");
            if (rs.next()) stats.put("Likes", rs.getInt(1));
            
            rs = stm.executeQuery("SELECT COUNT(*) FROM reaction WHERE type='dislike'");
            if (rs.next()) stats.put("Dislikes", rs.getInt(1));
            
        } catch (SQLException e) {
            System.err.println("Error fetching interaction type stats: " + e.getMessage());
        }
        return stats;
    }
}
