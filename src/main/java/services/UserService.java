package services;

import models.User;
import utils.MyDataBase;
import interfaces.IService;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserService implements IService<User> {

    private Connection cnx;

    public UserService() {
        this.cnx = MyDataBase.getInstance().getCnx();
        migrate();
    }

    public UserService(Connection cnx) {
        this.cnx = cnx;
    }

    private void migrate() {
        try {
            DatabaseMetaData md = cnx.getMetaData();
            ResultSet rsStatus = md.getColumns(null, null, "user", "status");
            if (!rsStatus.next()) {
                cnx.createStatement().execute("ALTER TABLE `user` ADD `status` VARCHAR(20) DEFAULT 'Actif'");
                System.out.println("--- Migration: 'status' column added ---");
            }
            ResultSet rsDate = md.getColumns(null, null, "user", "registration_date");
            if (!rsDate.next()) {
                cnx.createStatement()
                        .execute("ALTER TABLE `user` ADD `registration_date` TIMESTAMP DEFAULT CURRENT_TIMESTAMP");
                System.out.println("--- Migration: 'registration_date' column added ---");
            }
        } catch (SQLException e) {
            System.err.println("Migration error: " + e.getMessage());
        }
    }

    @Override
    public void add(User user) {
        String req = "INSERT INTO `user` (`email`, `password`, `role`, `nom`, `prenom`, `localisation`, `bio`, `specialite`, `telephone`, `status`, `registration_date`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            PreparedStatement pstm = cnx.prepareStatement(req);
            pstm.setString(1, user.getEmail());
            pstm.setString(2, user.getPassword());
            pstm.setString(3, user.getRole());
            pstm.setString(4, user.getNom());
            pstm.setString(5, user.getPrenom());
            pstm.setString(6, user.getLocalisation());
            pstm.setString(7, user.getBio());
            pstm.setString(8, user.getSpecialite());
            pstm.setString(9, user.getTelephone());
            pstm.setString(10, user.getStatus() != null ? user.getStatus() : "Actif");
            pstm.setTimestamp(11, user.getRegistrationDate() != null ? user.getRegistrationDate()
                    : new java.sql.Timestamp(System.currentTimeMillis()));
            pstm.executeUpdate();
            System.out.println("User ajouté avec succès !");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void update(User user) {
        String req = "UPDATE `user` SET `email`=?, `role`=?, `nom`=?, `prenom`=?, `localisation`=?, `bio`=?, `specialite`=?, `telephone`=?, `status`=? WHERE `id`=?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(req);
            pstm.setString(1, user.getEmail());
            pstm.setString(2, user.getRole());
            pstm.setString(3, user.getNom());
            pstm.setString(4, user.getPrenom());
            pstm.setString(5, user.getLocalisation());
            pstm.setString(6, user.getBio());
            pstm.setString(7, user.getSpecialite());
            pstm.setString(8, user.getTelephone());
            pstm.setString(9, user.getStatus());
            pstm.setInt(10, user.getId());
            pstm.executeUpdate();
            System.out.println("User modifié avec succès !");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void delete(User user) {
        String req = "DELETE FROM `user` WHERE `id`=?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(req);
            pstm.setInt(1, user.getId());
            pstm.executeUpdate();
            System.out.println("User supprimé avec succès !");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public List<User> getAll() {
        List<User> users = new ArrayList<>();
        String req = "SELECT * FROM `user` ORDER BY `registration_date` DESC";
        try {
            Statement st = cnx.createStatement();
            ResultSet rs = st.executeQuery(req);
            while (rs.next()) {
                User u = new User();
                u.setId(rs.getInt("id"));
                u.setEmail(rs.getString("email"));
                u.setPassword(rs.getString("password"));
                u.setRole(rs.getString("role"));
                u.setNom(rs.getString("nom"));
                u.setPrenom(rs.getString("prenom"));
                u.setLocalisation(rs.getString("localisation"));
                u.setBio(rs.getString("bio"));
                u.setSpecialite(rs.getString("specialite"));
                u.setTelephone(rs.getString("telephone"));
                u.setStatus(rs.getString("status"));
                u.setRegistrationDate(rs.getTimestamp("registration_date"));
                users.add(u);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return users;
    }

    public boolean existsByEmail(String email) {
        String req = "SELECT count(*) FROM `user` WHERE `email` = ?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(req);
            pstm.setString(1, email);
            ResultSet rs = pstm.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("existsByEmail error: " + e.getMessage());
        }
        return false;
    }

    public boolean isEmailTaken(String email, int currentUserId) {
        String req = "SELECT count(*) FROM `user` WHERE `email` = ? AND `id` != ?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(req);
            pstm.setString(1, email);
            pstm.setInt(2, currentUserId);
            ResultSet rs = pstm.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("isEmailTaken error: " + e.getMessage());
        }
        return false;
    }
}
