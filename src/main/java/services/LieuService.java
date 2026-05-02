package services;

import interfaces.IService;
import models.Lieu;
import utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LieuService implements IService<Lieu> {

    private Connection cnx;

    public LieuService() {
        this.cnx = MyDataBase.getInstance().getCnx();
        if (this.cnx != null) {
            migrate();
        } else {
            System.err.println("LieuService: Connection is null, skipping migration.");
        }
    }

    public LieuService(Connection cnx) {
        this.cnx = cnx;
    }

    private void migrate() {
        if (cnx == null) return;
        try {
            String sql = "CREATE TABLE IF NOT EXISTS `lieu` (" +
                    "`id` INT NOT NULL AUTO_INCREMENT," +
                    "`adresse` VARCHAR(255) NOT NULL," +
                    "`ville` VARCHAR(100) NOT NULL," +
                    "`capacite` INT NOT NULL," +
                    "`image` VARCHAR(500) DEFAULT NULL," +
                    "`disponibilite` TINYINT(1) NOT NULL DEFAULT 1," +
                    "`description` TEXT," +
                    "PRIMARY KEY (`id`)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";
            cnx.createStatement().execute(sql);
        } catch (SQLException e) {
            System.err.println("Migration lieu error: " + e.getMessage());
        }
    }

    @Override
    public void add(Lieu lieu) {
        if (cnx == null) return;
        String req = "INSERT INTO `lieu` (`adresse`, `ville`, `capacite`, `image`, `disponibilite`, `description`) VALUES (?, ?, ?, ?, ?, ?)";
        try {
            PreparedStatement pstm = cnx.prepareStatement(req);
            pstm.setString(1, lieu.getAdresse());
            pstm.setString(2, lieu.getVille());
            pstm.setInt(3, lieu.getCapacite());
            pstm.setString(4, lieu.getImage());
            pstm.setInt(5, lieu.isDisponibilite() ? 1 : 0);
            pstm.setString(6, lieu.getDescription());
            pstm.executeUpdate();
            System.out.println("Lieu ajouté avec succès !");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void update(Lieu lieu) {
        if (cnx == null) return;
        String req = "UPDATE `lieu` SET `adresse`=?, `ville`=?, `capacite`=?, `image`=?, `disponibilite`=?, `description`=? WHERE `id`=?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(req);
            pstm.setString(1, lieu.getAdresse());
            pstm.setString(2, lieu.getVille());
            pstm.setInt(3, lieu.getCapacite());
            pstm.setString(4, lieu.getImage());
            pstm.setInt(5, lieu.isDisponibilite() ? 1 : 0);
            pstm.setString(6, lieu.getDescription());
            pstm.setInt(7, lieu.getId());
            pstm.executeUpdate();
            System.out.println("Lieu modifié avec succès !");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void delete(Lieu lieu) {
        if (cnx == null) return;
        try {
            // First delete related events to handle foreign key constraint manual cascade
            String deleteEvents = "DELETE FROM `evenement` WHERE `lieu_id`=?";
            PreparedStatement pstmE = cnx.prepareStatement(deleteEvents);
            pstmE.setInt(1, lieu.getId());
            pstmE.executeUpdate();

            // Then delete the lieu
            String req = "DELETE FROM `lieu` WHERE `id`=?";
            PreparedStatement pstm = cnx.prepareStatement(req);
            pstm.setInt(1, lieu.getId());
            pstm.executeUpdate();
            System.out.println("Lieu supprimé avec succès !");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public List<Lieu> getAll() {
        List<Lieu> list = new ArrayList<>();
        if (cnx == null) return list;
        String req = "SELECT * FROM `lieu` ORDER BY `ville` ASC, `id` ASC";
        try {
            Statement st = cnx.createStatement();
            ResultSet rs = st.executeQuery(req);
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return list;
    }

    public Lieu getById(int id) {
        if (cnx == null) return null;
        String req = "SELECT * FROM `lieu` WHERE `id`=?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(req);
            pstm.setInt(1, id);
            ResultSet rs = pstm.executeQuery();
            if (rs.next()) {
                return mapRow(rs);
            }
        } catch (SQLException e) {
            System.err.println("getById lieu error: " + e.getMessage());
        }
        return null;
    }

    private Lieu mapRow(ResultSet rs) throws SQLException {
        Lieu l = new Lieu();
        l.setId(rs.getInt("id"));
        l.setAdresse(rs.getString("adresse"));
        l.setVille(rs.getString("ville"));
        l.setCapacite(rs.getInt("capacite"));
        l.setImage(rs.getString("image"));
        l.setDisponibilite(rs.getInt("disponibilite") == 1);
        l.setDescription(rs.getString("description"));
        return l;
    }
}
