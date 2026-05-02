package services;

import interfaces.IService;
import models.Offre;
import utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class OffreService implements IService<Offre> {
    private Connection cnx;

    public OffreService() {
        this.cnx = MyDataBase.getInstance().getCnx();
        if (this.cnx != null) {
            migrate();
        } else {
            System.err.println("OffreService: Connection is null, skipping migration.");
        }
    }

    public OffreService(Connection cnx) {
        this.cnx = cnx;
    }

    private void migrate() {
        if (cnx == null) return;
        try {
            String sql = "CREATE TABLE IF NOT EXISTS `offre` (" +
                    "`id` INT NOT NULL AUTO_INCREMENT," +
                    "`telephone` VARCHAR(30) NOT NULL," +
                    "`categorie` VARCHAR(100) NOT NULL," +
                    "`description` TEXT," +
                    "`photo` VARCHAR(500) DEFAULT NULL," +
                    "`quantite` INT NOT NULL DEFAULT 0," +
                    "PRIMARY KEY (`id`)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";
            cnx.createStatement().execute(sql);
        } catch (SQLException e) {
            System.err.println("Migration offre error: " + e.getMessage());
        }
    }

    @Override
    public void add(Offre offre) {
        if (cnx == null) return;
        String req = "INSERT INTO `offre` (`telephone`, `categorie`, `description`, `photo`, `quantite`) VALUES (?, ?, ?, ?, ?)";
        try {
            PreparedStatement pstm = cnx.prepareStatement(req);
            pstm.setString(1, offre.getTelephone());
            pstm.setString(2, offre.getCategorie());
            pstm.setString(3, offre.getDescription());
            pstm.setString(4, offre.getPhoto());
            pstm.setInt(5, offre.getQuantite());
            pstm.executeUpdate();
            System.out.println("Offre ajoutee avec succes !");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void update(Offre offre) {
        if (cnx == null) return;
        String req = "UPDATE `offre` SET `telephone`=?, `categorie`=?, `description`=?, `photo`=?, `quantite`=? WHERE `id`=?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(req);
            pstm.setString(1, offre.getTelephone());
            pstm.setString(2, offre.getCategorie());
            pstm.setString(3, offre.getDescription());
            pstm.setString(4, offre.getPhoto());
            pstm.setInt(5, offre.getQuantite());
            pstm.setInt(6, offre.getId());
            pstm.executeUpdate();
            System.out.println("Offre modifiee avec succes !");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void delete(Offre offre) {
        if (cnx == null) return;
        String req = "DELETE FROM `offre` WHERE `id`=?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(req);
            pstm.setInt(1, offre.getId());
            pstm.executeUpdate();
            System.out.println("Offre supprimee avec succes !");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public List<Offre> getAll() {
        List<Offre> offres = new ArrayList<>();
        if (cnx == null) return offres;
        String req = "SELECT * FROM `offre` ORDER BY `id` DESC";
        try {
            Statement st = cnx.createStatement();
            ResultSet rs = st.executeQuery(req);
            while (rs.next()) {
                offres.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return offres;
    }

    public Offre getById(int id) {
        if (cnx == null) return null;
        String req = "SELECT * FROM `offre` WHERE `id`=?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(req);
            pstm.setInt(1, id);
            ResultSet rs = pstm.executeQuery();
            if (rs.next()) {
                return mapRow(rs);
            }
        } catch (SQLException e) {
            System.err.println("getById offre error: " + e.getMessage());
        }
        return null;
    }

    private Offre mapRow(ResultSet rs) throws SQLException {
        Offre offre = new Offre();
        offre.setId(rs.getInt("id"));
        offre.setTelephone(rs.getString("telephone"));
        offre.setCategorie(rs.getString("categorie"));
        offre.setDescription(rs.getString("description"));
        offre.setPhoto(rs.getString("photo"));
        offre.setQuantite(rs.getInt("quantite"));
        return offre;
    }

    public Map<String, Integer> getDonorsStatistics() {
        Map<String, Integer> stats = new LinkedHashMap<>();
        if (cnx == null) return stats;
        String req = "SELECT telephone, SUM(quantite) as total_quantite FROM `offre` GROUP BY telephone ORDER BY total_quantite DESC";
        try {
            Statement st = cnx.createStatement();
            ResultSet rs = st.executeQuery(req);
            while (rs.next()) {
                stats.put(rs.getString("telephone"), rs.getInt("total_quantite"));
            }
        } catch (SQLException e) {
            System.err.println("getDonorsStatistics error: " + e.getMessage());
        }
        return stats;
    }
}
