package services;

import utils.MyDataBase;

import java.sql.*;

public class ServiceReaction {

    private Connection cnx;

    public ServiceReaction() {
        utils.DatabaseInitializer.initialize();
        cnx = MyDataBase.getInstance().getCnx();
    }

    /**
     * Toggle a like or dislike.
     * - If user has no reaction → insert it.
     * - If user has the SAME type → remove it (un-react).
     * - If user has the OPPOSITE type → switch to the new type.
     *
     * @param userId     the acting user
     * @param targetType "publication" or "commentaire"
     * @param targetId   id of the publication or comment
     * @param type       "like" or "dislike"
     */
    public void toggleReaction(int userId, String targetType, int targetId, String type) {
        String existing = getUserReaction(userId, targetType, targetId);

        if (existing == null) {
            // No reaction yet → insert
            String sql = "INSERT INTO `reaction` (`user_id`, `target_type`, `target_id`, `type`) VALUES (?, ?, ?, ?)";
            try (PreparedStatement pstm = cnx.prepareStatement(sql)) {
                pstm.setInt(1, userId);
                pstm.setString(2, targetType);
                pstm.setInt(3, targetId);
                pstm.setString(4, type);
                pstm.executeUpdate();
            } catch (SQLException e) {
                System.err.println("Reaction insert error: " + e.getMessage());
            }
        } else if (existing.equals(type)) {
            // Same type → remove (toggle off)
            String sql = "DELETE FROM `reaction` WHERE `user_id`=? AND `target_type`=? AND `target_id`=?";
            try (PreparedStatement pstm = cnx.prepareStatement(sql)) {
                pstm.setInt(1, userId);
                pstm.setString(2, targetType);
                pstm.setInt(3, targetId);
                pstm.executeUpdate();
            } catch (SQLException e) {
                System.err.println("Reaction delete error: " + e.getMessage());
            }
        } else {
            // Opposite type → switch
            String sql = "UPDATE `reaction` SET `type`=? WHERE `user_id`=? AND `target_type`=? AND `target_id`=?";
            try (PreparedStatement pstm = cnx.prepareStatement(sql)) {
                pstm.setString(1, type);
                pstm.setInt(2, userId);
                pstm.setString(3, targetType);
                pstm.setInt(4, targetId);
                pstm.executeUpdate();
            } catch (SQLException e) {
                System.err.println("Reaction update error: " + e.getMessage());
            }
        }
    }

    /**
     * Count likes for a target.
     */
    public int countLikes(String targetType, int targetId) {
        return countByType(targetType, targetId, "like");
    }

    /**
     * Count dislikes for a target.
     */
    public int countDislikes(String targetType, int targetId) {
        return countByType(targetType, targetId, "dislike");
    }

    private int countByType(String targetType, int targetId, String type) {
        String sql = "SELECT COUNT(*) FROM `reaction` WHERE `target_type`=? AND `target_id`=? AND `type`=?";
        try (PreparedStatement pstm = cnx.prepareStatement(sql)) {
            pstm.setString(1, targetType);
            pstm.setInt(2, targetId);
            pstm.setString(3, type);
            ResultSet rs = pstm.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("Reaction count error: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Returns the current user's reaction: "like", "dislike", or null.
     */
    public String getUserReaction(int userId, String targetType, int targetId) {
        String sql = "SELECT `type` FROM `reaction` WHERE `user_id`=? AND `target_type`=? AND `target_id`=?";
        try (PreparedStatement pstm = cnx.prepareStatement(sql)) {
            pstm.setInt(1, userId);
            pstm.setString(2, targetType);
            pstm.setInt(3, targetId);
            ResultSet rs = pstm.executeQuery();
            if (rs.next()) return rs.getString("type");
        } catch (SQLException e) {
            System.err.println("Reaction get error: " + e.getMessage());
        }
        return null;
    }
}
