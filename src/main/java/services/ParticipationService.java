package services;

import utils.MyDataBase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class ParticipationService {

    private final Connection cnx;
    private static final String TABLE = "participation_evenement";

    public ParticipationService() {
        this.cnx = MyDataBase.getInstance().getCnx();
        migrate();
    }

    public ParticipationService(Connection cnx) {
        this.cnx = cnx;
        migrate();
    }

    private void migrate() {
        if (cnx == null) return;
        try {
            String sql = "CREATE TABLE IF NOT EXISTS `" + TABLE + "` (" +
                    "`id` INT NOT NULL AUTO_INCREMENT," +
                    "`evenement_id` INT NOT NULL," +
                    "`user_id` INT NOT NULL," +
                    "`created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                    "PRIMARY KEY (`id`)," +
                    "UNIQUE KEY `uk_participation_event_user` (`evenement_id`, `user_id`)," +
                    "KEY `idx_participation_event` (`evenement_id`)," +
                    "CONSTRAINT `fk_participation_evenement` FOREIGN KEY (`evenement_id`) REFERENCES `evenement` (`id`) ON DELETE CASCADE ON UPDATE CASCADE," +
                    "CONSTRAINT `fk_participation_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";
            Statement st = cnx.createStatement();
            st.execute(sql);
        } catch (SQLException e) {
            System.err.println("Migration participation error: " + e.getMessage());
        }
    }

    public int countParticipantsByEvenement(int evenementId) {
        if (cnx == null) return 0;
        String req = "SELECT COUNT(*) FROM `" + TABLE + "` WHERE `evenement_id`=?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(req);
            pstm.setInt(1, evenementId);
            ResultSet rs = pstm.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("countParticipantsByEvenement error: " + e.getMessage());
        }
        return 0;
    }

    public boolean hasParticipation(int evenementId, int userId) {
        if (cnx == null) return false;
        String req = "SELECT 1 FROM `" + TABLE + "` WHERE `evenement_id`=? AND `user_id`=? LIMIT 1";
        try {
            PreparedStatement pstm = cnx.prepareStatement(req);
            pstm.setInt(1, evenementId);
            pstm.setInt(2, userId);
            ResultSet rs = pstm.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            System.err.println("hasParticipation error: " + e.getMessage());
            return false;
        }
    }

    public boolean addParticipation(int evenementId, int userId) {
        if (cnx == null) return false;
        String req = "INSERT INTO `" + TABLE + "` (`evenement_id`, `user_id`, `created_at`) VALUES (?, ?, CURRENT_TIMESTAMP)";
        try {
            PreparedStatement pstm = cnx.prepareStatement(req);
            pstm.setInt(1, evenementId);
            pstm.setInt(2, userId);
            pstm.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("addParticipation error: " + e.getMessage());
            return false;
        }
    }

    public java.util.List<models.User> getParticipantsByEvenement(int evenementId) {
        java.util.List<models.User> list = new java.util.ArrayList<>();
        if (cnx == null) return list;
        String req = "SELECT u.* FROM `user` u " +
                     "JOIN `" + TABLE + "` p ON p.`user_id` = u.`id` " +
                     "WHERE p.`evenement_id` = ?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(req);
            pstm.setInt(1, evenementId);
            ResultSet rs = pstm.executeQuery();
            while (rs.next()) {
                models.User u = new models.User();
                u.setId(rs.getInt("id"));
                u.setNom(rs.getString("nom"));
                u.setPrenom(rs.getString("prenom"));
                u.setEmail(rs.getString("email"));
                u.setTelephone(rs.getString("telephone"));
                u.setRole(rs.getString("role"));
                list.add(u);
            }
        } catch (SQLException e) {
            System.err.println("getParticipantsByEvenement error: " + e.getMessage());
        }
        return list;
    }
}
